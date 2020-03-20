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
import java.util.Map;

// check status, to export the device status
public class CheckBlot extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(CheckBlot.class);
    private Gson gson;
    private OutputCollector outputCollector;


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


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
