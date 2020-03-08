package core;

import config.AppConfig;
import msg.ControlMsg;
import msg.ModelMsg;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ModelBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(ModelBolt.class);
    private OutputCollector outputCollector;

    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }

    private ControlMsg callModel(ModelMsg msg) {
        ControlMsg controlMsg = new ControlMsg();
        Map<String, Object> params = new HashMap<>();
        params.put("time", msg.getTime());
        params.put("brand", msg.getBrand());
        params.put("batch", msg.getBatch());
        params.put("index", msg.getIndex());
        params.put("stage", msg.getStage());
        params.put("features", msg.generate());
        Response response = null;
        try {
            response = AppUtil.doPost(AppConfig.ModelServerConfig.modelUrl, String.valueOf(new JSONObject(params)));
            JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());
            controlMsg.setBatch(json.getString("batch"));
            controlMsg.setBrand(json.getString("brand"));
            controlMsg.setTime(json.getLong("time"));
            controlMsg.setVersion(json.getString("version"));
            controlMsg.setDeviceStatus(json.getString("deviceStatus"));
            controlMsg.setTempRegion1((float) json.getDouble("tempRegion1"));
            controlMsg.setTempRegion2((float) json.getDouble("tempRegion2"));

            logger.info("predict value = " + controlMsg.getTempRegion1() + " " + controlMsg.getTempRegion2());
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return controlMsg;
    }

    /**
     * 从 FeatureComputeBolt 获取特征计算结果，调用模型预测，将控制信息传递给下游
     */
    public void execute(Tuple tuple) {
        ModelMsg modelMsg = (ModelMsg) tuple.getValue(0);
        ControlMsg controlMsg = callModel(modelMsg);

        saveControlMsg(controlMsg);

        outputCollector.emit(new Values(controlMsg));
        outputCollector.ack(tuple);

    }

    private void saveControlMsg(ControlMsg controlMsg) {
        // TODO 保存控制消息
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("model"));
    }
}
