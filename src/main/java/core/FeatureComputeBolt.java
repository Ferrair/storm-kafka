package core;


import msg.IoTMsg;
import msg.ModelMsg;
import msg.ProcessMsg;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FeatureUtil;

import java.util.*;

public class FeatureComputeBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(FeatureComputeBolt.class);
    private OutputCollector outputCollector;

    public FeatureComputeBolt() {

    }

    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }


    private ModelMsg computeFeature(List<IoTMsg> window, int windowSize, int blockSize) {
        if (window.size() != windowSize) {
            return null;
        }
        /*
         * 计算数学统计量
         */
        ModelMsg modelMsg = new ModelMsg();
        List<List<IoTMsg>> splits = split(window, windowSize, blockSize);
        List<List<IoTMsg>> features = calculate(splits);
        merge(modelMsg, features);

        /*
         * 加入原始信息
         *
         */
        addOriginalFeature(modelMsg, window);

        return modelMsg;
    }

    private void addOriginalFeature(ModelMsg modelMsg, List<IoTMsg> window) {
        List<Double> humidDiffOriginal = new ArrayList<>();
        for (IoTMsg msg : window) {
            humidDiffOriginal.add(msg.getHumidOutDiff());
        }
        modelMsg.setHumidDiffOriginal(humidDiffOriginal);
    }


    /**
     * 构造为模型需要的特征
     */
    private void merge(ModelMsg modelMsg, List<List<IoTMsg>> features) {
        List<Double> meanList = new ArrayList<>();
        List<Double> stdList = new ArrayList<>();
        List<Double> integralList = new ArrayList<>();
        List<Double> skewList = new ArrayList<>();
        List<Double> kurtosisList = new ArrayList<>();
        for (List<IoTMsg> feature : features) {
            IoTMsg mean = feature.get(0);
            IoTMsg std = feature.get(1);
            IoTMsg integral = feature.get(2);
            IoTMsg skew = feature.get(3);
            IoTMsg kurtosis = feature.get(4);

            meanList.addAll(Arrays.asList(mean.generate()));
            stdList.addAll(Arrays.asList(std.generate()));
            integralList.addAll(Arrays.asList(integral.generate()));
            skewList.addAll(Arrays.asList(skew.generate()));
            kurtosisList.addAll(Arrays.asList(kurtosis.generate()));
        }
        modelMsg.setMean(meanList);
        modelMsg.setStd(stdList);
        modelMsg.setIntegral(integralList);
        modelMsg.setSkew(skewList);
        modelMsg.setKurtosis(kurtosisList);
    }

    /**
     * 对于每一个split进行特征计算
     */
    private List<List<IoTMsg>> calculate(List<List<IoTMsg>> splits) {
        List<List<IoTMsg>> features = new ArrayList<>();
        for (List<IoTMsg> oneSplit : splits) {
            List<IoTMsg> featurePerSplit = new ArrayList<>();
            IoTMsg[] oneSplitArray = oneSplit.toArray(new IoTMsg[0]);

            IoTMsg mean = FeatureUtil.calcMean(oneSplitArray);
            featurePerSplit.add(mean);
            featurePerSplit.add(FeatureUtil.calcStd(oneSplitArray, mean));
            featurePerSplit.add(FeatureUtil.calcIntegral(oneSplitArray));
            featurePerSplit.add(FeatureUtil.calcSkew(oneSplitArray, mean));
            featurePerSplit.add(FeatureUtil.calcKurtosis(oneSplitArray, mean));

            features.add(featurePerSplit);
        }
        return features;
    }

    /**
     * 将 windowSize 长度的数据进行划分
     */
    private List<List<IoTMsg>> split(List<IoTMsg> window, int windowSize, int blockSize) {
        List<List<IoTMsg>> splits = new ArrayList<>();
        if (windowSize == 0) {
            return splits;
        }

        int splitsNum = windowSize / blockSize;
        for (int i = 0; i < splitsNum; i++) {
            splits.add(window.subList(blockSize * i, blockSize * (i + 1)));
        }
        return splits;
    }


    /**
     * 从 WindowBolt 处接收窗口并计算特征，将计算结果传递给下游
     *
     * @param tuple
     */
    public void execute(Tuple tuple) {
        ProcessMsg processMsg = (ProcessMsg) tuple.getValue(0);

        ModelMsg modelMsg;
        if (processMsg.getStage().equals("head")) {
            modelMsg = new ModelMsg();
        } else {
            modelMsg = computeFeature(processMsg.getWindow(), processMsg.getWindowSize(), processMsg.getBlockSize());
            if (modelMsg == null) {
                return;
            }
        }

        modelMsg.setStage(processMsg.getStage());
        modelMsg.setBrand(processMsg.getBrand());
        modelMsg.setIndex(processMsg.getIndex());
        modelMsg.setBatch(processMsg.getBatch());
        modelMsg.setDeviceStatus(processMsg.getDeviceStatus());
        modelMsg.setTime(processMsg.getTime());
        modelMsg.setWindowTime(processMsg.getWindowTime());
        modelMsg.setKafkaTime(processMsg.getKafkaTime());
        modelMsg.setModelTime(System.currentTimeMillis());

        // logger.info("Feature: " + processMsg.getTime() + " " + processMsg.toString());
        // logger.info("After Feature: " + modelMsg.getTime() + " " + modelMsg.generate());

        saveModelMsg(modelMsg);

        outputCollector.emit(new Values(modelMsg));
        outputCollector.ack(tuple);
    }

    private void saveModelMsg(ModelMsg modelMsg) {
        // TODO 保存处理后的Msg
    }


    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("features"));
    }
}
