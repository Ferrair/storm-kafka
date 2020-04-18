package core;

import gherkin.deps.com.google.gson.Gson;
import config.AppConfig;
import gherkin.deps.com.google.gson.JsonSyntaxException;
import msg.IoTMsg;
import msg.OriginalMsg;
import msg.ProcessMsg;
import okhttp3.Response;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.AppUtil;
import util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 用于维护窗口大小的 Bolt
 * 从 kafka 处接收数据，并将数据维护在 window 内
 */
public class WindowsBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(WindowsBolt.class);
    private OutputCollector outputCollector;
    /**
     * 将JSON数据映射为Object
     */
    private Gson gson;
    /**
     * 为了一个队列，先进先出，队列长度始终不变
     */
    private Queue<IoTMsg> window = new LinkedList<>();
    /**
     * 当前点位对应的Batch
     */
    private String currentBatch = null;
    /**
     * 记录当前点位在该Batch的index，index=1为第一个点
     */
    private long startIndex = 0;
    /**
     * 记录上一个点位的时间戳，当前点位时间戳 小于 上一个点位的时间戳，会报错
     */
    private long lastTimestamp = 0L;
    /**
     * 从模型得到的切分配置
     */
    private int[] modelConfig = new int[2];

    /**
     * 判断是否ack
     */
    private final boolean ack = true;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        this.gson = new Gson();
    }

    /*
     * TODO: untested
     * 在 startIndex == 1的时候进行请求，即批次开始的时候
     */
    private int[] getWindowSize(String stage) {
        if (stage.equals("head")) {
            return new int[]{1, 1};
        }
        if (startIndex != 1) {
            return modelConfig;
        }
        Response response = null;
        try {
            Map<String, Object> query = new HashMap<>();
            query.put("stage", stage);
            response = AppUtil.doGet(
                    AppConfig.ModelServerConfig.modelConfigUrl,
                    new HashMap<>(),
                    query
            );
            JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());
            modelConfig[0] = json.getInt("window_size");
            modelConfig[1] = json.getInt("block_size");

            // logger.info("window_size=" + modelConfig[0] + " block_size=" + modelConfig[1] + " and index=" + startIndex);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return modelConfig;
    }


    /**
     * 解析 tuple 里面的参数信息
     * 得到 一个String
     * i = 4：根据Kafka的传输过来的数据来确定
     */
    private String parseTuple(Tuple tuple) {
        return tuple.getValue(4).toString();
    }


    /**
     * 根据 index 判断 stage
     */
    private String determineStage(OriginalMsg msg) {
        int deviceStatus = msg.getDeviceStatus();

        if (startIndex < 200 && deviceStatus != 32) {
            return "head";
        } else if (startIndex < 400 && deviceStatus != 32) {
            return "transition";
        } else {
            return "produce";
        }
    }


    /**
     * TODO 判断当Kafka没有数据的情况
     */
    private boolean checkTimeStamp(long currentTimestamp) {
        if (currentTimestamp >= lastTimestamp || lastTimestamp == 0) {
            lastTimestamp = currentTimestamp;
            return true;
        }
        return false;
    }

    /**
     * 判断当前Msg在整个批次中的Index
     * 1. 如果当前Msg的流量累计量为0 -> 0
     * 2. 之前没有批次在生产, 或者当前批次和之前批次不一样 -> 0
     */
    private long determineIndex(OriginalMsg msg) {
        if (msg.isBatchStart() || currentBatch == null || !currentBatch.equals(msg.getBatch())) {
            currentBatch = msg.getBatch();
            startIndex = 0;
            return startIndex++;
        }
        return startIndex++;
    }

    @Override
    public void execute(Tuple tuple) {
        String str = parseTuple(tuple);
        OriginalMsg originalMsg = null;
        try {
            originalMsg = gson.fromJson(str, OriginalMsg.class);
        } catch (JsonSyntaxException e) {
            logger.error(e.getMessage() + " " + tuple.toString());
        }
        // str 并不是一个 OriginalMsg
        if (originalMsg == null) {
            if (ack) {
                outputCollector.ack(tuple);
            }
            return;
        }
        // 不是生产状态，清空队列后返回
        try {
            if (!OriginalMsg.isInProductMode(originalMsg.getDeviceStatus())) {
                logger.info("Current data is not in product mode: " + str);
                window.clear();
                if (ack) {
                    outputCollector.ack(tuple);
                }
                return;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }


        // 检查时间戳
        long time = originalMsg.getTimestamp();
        if (!checkTimeStamp(time)) {
            logger.error("Current timestamp  " + time + " is smaller than last timestamp " + lastTimestamp);
            if (ack) {
                outputCollector.ack(tuple);
            }
            return;
        }

        Double[] array = originalMsg.generate();
        if (array.length == 0) {
            if (ack) {
                outputCollector.ack(tuple);
            }
            return;
        }

        String stage = determineStage(originalMsg);
        long index = determineIndex(originalMsg);
        String brand = originalMsg.getBrand();
        String deviceStatus = String.valueOf(originalMsg.getDeviceStatus());
        String batch = originalMsg.getBatch();
        int[] modelConfig = getWindowSize(stage);

        IoTMsg msg = new IoTMsg(array);

        window.add(msg);
        while (window.size() > modelConfig[0]) {
            window.poll();
        }
        if (window.size() == modelConfig[0]) {
            ProcessMsg processMsg = new ProcessMsg();
            processMsg.setWindowSize(modelConfig[0]);
            processMsg.setBlockSize(modelConfig[1]);
            processMsg.setIndex(index);
            processMsg.setBatch(batch);
            processMsg.setBrand(brand);
            processMsg.setStage(stage);
            processMsg.setDeviceStatus(deviceStatus);
            processMsg.setTime(time);
            // need copy a new list for further use
            // otherwise, cause multiple threads problems
            processMsg.setWindow(new ArrayList<>(window));
            outputCollector.emit(new Values(processMsg));
        }
        if (ack) {
            outputCollector.ack(tuple);
        }
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("window"));
    }
}
