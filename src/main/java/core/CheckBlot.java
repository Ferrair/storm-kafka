package core;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.JsonSyntaxException;
import msg.OriginalMsg;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// check status, to export the device status
public class CheckBlot extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(CheckBlot.class);
    private Gson gson;
    private OutputCollector outputCollector;
    private int warningCounter = 0;
    private static final int N = 10;


    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.gson = new Gson();
        this.outputCollector = outputCollector;
    }


    public String parseTuple(Tuple tuple) {
        return tuple.getValue(4).toString();
    }


    public String timestamp2Str(long timestamp, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public long str2Timestamp(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public void execute(Tuple tuple) {
        String str = null;
        try {
            str = parseTuple(tuple);
        } catch (Exception e) {
            logger.info(e.getMessage());
            outputCollector.ack(tuple);
            return;
        }

        OriginalMsg originalMsg = null;

        try {
            originalMsg = gson.fromJson(str, OriginalMsg.class);
        } catch (JsonSyntaxException e) {
            logger.error(e.getMessage() + " " + tuple.toString());
            outputCollector.ack(tuple);
            return;
        }

        long timestamp = originalMsg.getTimestamp();

        checkInputValue(originalMsg);

//        if (timestamp >= str2Timestamp("2020-03-12 00:00:00")) {
//            logger.info(timestamp2Str(timestamp, "yyyy-MM-dd HH:mm:ss"));
//        }
//        if (timestamp >= str2Timestamp("2020-02-28 00:00:00") && timestamp < str2Timestamp("2020-02-29 00:00:00")
//                || timestamp >= str2Timestamp("2020-03-05 00:00:00") && timestamp < str2Timestamp("2020-03-07 00:00:00")
//        || timestamp >= str2Timestamp("2020-03-08 00:00:00") && timestamp < str2Timestamp("2020-03-09 00:00:00")) {
//            StringBuffer sb = new StringBuffer();
//            sb.append(timestamp2Str(timestamp, "yyyy-MM-dd HH:mm:ss"))
//                    .append(",")
//                    .append(originalMsg.getDeviceStatus());
//            try {
//                FileUtil.append(sb.toString(), "check.csv");
//            } catch (Exception e) {
//                outputCollector.ack(tuple);
//                e.printStackTrace();
//            }
//        }
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp2Str(timestamp, "yyyy-MM-dd HH:mm:ss"))
                .append(" --- ")
                .append(str);
        try {
            FileUtil.append(sb.toString(), "check.csv");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputCollector.ack(tuple);
        }
    }

    private void checkInputValue(OriginalMsg originalMsg) {
        if (originalMsg == null) {
            return;
        }
        String brand = originalMsg.getBrand();
        Double[] array = originalMsg.generate();
        if (brand.equals("")) {
            return;
        }
        if (array.length == 0) {
            return;
        }
        Double humidOut = array[2];
        Double humidIn = array[3];
        Double tempActual1 = array[5];
        Double tempSetting1 = array[6];
        Double tempActual2 = array[7];
        Double tempSetting2 = array[8];

        if (Math.abs(humidOutCriterion.get(brand) - humidOut) >= 0.1) {
            sendWarningInfo(brand + " Humid Out In Warning: " + humidOut);
            warningCounter++;
        }
        if (Math.abs(tempSetting1Criterion.get(brand) - tempSetting1) >= 5) {
            sendWarningInfo(brand + " Temp Setting1 In Warning: " + tempSetting1);
            warningCounter++;
        }
        if (Math.abs(tempSetting2Criterion.get(brand) - tempSetting2) >= 5) {
            sendWarningInfo(brand + "Temp Setting2 In Warning: " + tempSetting2);
            warningCounter++;
        }

        if (warningCounter >= N) {
            throw new IllegalStateException("Model failure, please contract administrator");
        }
    }

    private void sendWarningInfo(String info) {

    }

    private static Map<String, Double> humidOutCriterion = new HashMap<>();
    private static Map<String, Double> tempSetting1Criterion = new HashMap<>();
    private static Map<String, Double> tempSetting2Criterion = new HashMap<>();

    static {
        humidOutCriterion.put("Txy###", 12.7);
        humidOutCriterion.put("TG####A", 12.5);
        humidOutCriterion.put("HSX###", 13.8);
        humidOutCriterion.put("TH####A", 12.5);
        humidOutCriterion.put("DQMr##", 13.8);
        humidOutCriterion.put("ThQD##A", 12.5);
        humidOutCriterion.put("HsxY##", 13.5);
        humidOutCriterion.put("HR####", 12.8);

        tempSetting1Criterion.put("Txy###", 135d);
        tempSetting1Criterion.put("TG####A", 140d);
        tempSetting1Criterion.put("HSX###", 135d);
        tempSetting1Criterion.put("TH####A", 135d);
        tempSetting1Criterion.put("DQMr##", 130d);
        tempSetting1Criterion.put("ThQD##A", 135d);
        tempSetting1Criterion.put("HsxY##", 127d);
        tempSetting1Criterion.put("HR####", 145d);

        tempSetting2Criterion.put("Txy###", 120d);
        tempSetting2Criterion.put("TG####A", 125d);
        tempSetting2Criterion.put("HSX###", 135d);
        tempSetting2Criterion.put("TH####A", 121d);
        tempSetting2Criterion.put("DQMr##", 130d);
        tempSetting2Criterion.put("ThQD##A", 120d);
        tempSetting2Criterion.put("HsxY##", 127d);
        tempSetting2Criterion.put("HR####", 145d);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

}
