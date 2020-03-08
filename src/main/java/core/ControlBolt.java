package core;

import config.AppConfig;
import msg.ControlMsg;
import okhttp3.Response;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.AppUtil;

import java.util.*;

public class ControlBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(ControlBolt.class);
    private OutputCollector outputCollector;

    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }


    private void callControl(ControlMsg controlMsg) {
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> region1 = new HashMap<>();
        region1.put("Address", "5H.5H.K1_Z7TT1VALUE_1");
        region1.put("Value", controlMsg.getTempRegion1());
        region1.put("DataType", "float");

        Map<String, Object> region2 = new HashMap<>();
        region2.put("Address", "5H.5H.K1_Z7TT1VALUE_4");
        region2.put("Value", controlMsg.getTempRegion2());
        region2.put("DataType", "float");

        params.add(region1);
        params.add(region2);

        Response response = null;
        try {
            response = AppUtil.doPost(AppConfig.ControlServerConfig.controlUrl, String.valueOf(new JSONArray(params)));
            JSONArray json = new JSONArray(Objects.requireNonNull(response.body()).string());
            if (!json.getJSONObject(0).getBoolean("IsSetSuccessful") || !json.getJSONObject(1).getBoolean("IsSetSuccessful")) {
                logger.error("PLC set false, please try again");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * 从 ModelBolt 或取控制信息，调用反向控制 api
     */
    public void execute(Tuple tuple) {
        ControlMsg controlMsg = (ControlMsg) tuple.getValue(0);
        callControl(controlMsg);
        outputCollector.ack(tuple);
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("control"));
    }
}
