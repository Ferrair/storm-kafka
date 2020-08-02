package msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class DirectModelMsg {
    private Map<String, String> msgMap;
    private static final Map<Integer, String> deviceStatusMapping = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(OriginalMsg.class);
    private static int requiredCount;

    public Map<String, String> getMsgMap() {
        return msgMap;
    }

    public DirectModelMsg() {
        msgMap = genEmptyMap();
        requiredCount = msgMap.size();
        deviceStatusMapping.put(8, "准备");
        deviceStatusMapping.put(16, "启动");
        deviceStatusMapping.put(32, "生产");
        deviceStatusMapping.put(64, "收尾");

    }

    public static Map<String, String> genEmptyMap() {
        Map<String, String> map = new HashMap<>();
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("plc_keys");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String str = reader.readLine();
                if (str != null)
                    map.put(str, null);
                else
                    break;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public int getDeviceStatus() {
        if (msgMap.size() == 0) {
            return 1;
        }

        try {
            double deviceStatus1 = 1d, deviceStatus2 = 0d;
            for (Map.Entry<String, String> entry : msgMap.entrySet()) {
                if (entry.getKey().equals("5H.5H.LD5_KL2226_PHASE1")) {
                    deviceStatus1 = Double.parseDouble(entry.getValue());
                }
                if (entry.getKey().equals("5H.5H.LD5_KL2226_PHASE2")) {
                    deviceStatus2 = Double.parseDouble(entry.getValue());
                }
            }

            return (int) (deviceStatus1 + deviceStatus2 * 256);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 1;
        }
    }

    private double getFlowAcc() {
        try {
            if (msgMap.size() == 0) {
                return 1;
            }
            for (Map.Entry<String, String> entry : msgMap.entrySet()) {
                if (entry.getKey().equals("5H.5H.LD5_CK2222_TbcLeafFlowSH")) {
                    return Double.parseDouble(entry.getValue());
                }
            }
            return -1d;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return -1d;
        }
    }

    public String getBatch() {
        try {
            if (msgMap.size() == 0) {
                return "";
            }
            for (Map.Entry<String, String> entry : msgMap.entrySet()) {
                if (entry.getKey().equals("6032.6032.LD5_YT603_2B_YS2ROASTBATCHNO")) {
                    return entry.getValue();
                }
            }
            return "";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    public boolean isBatchStart() {
        return getFlowAcc() == 0;
    }

    public boolean isInProductMode(int deviceStatus) {
        return deviceStatusMapping.containsKey(deviceStatus);
    }

    public boolean genMapFromOrigin(OriginalMsg oMsg){
        try {
            int count = 0;
            for (OriginalMsg.SensorMsg sensorMsg : oMsg.getValues()) {
                String key = (String) sensorMsg.id;

                // 数据重复
                if(msgMap.get(key) != null){
                    throw new Exception("key:" + key + " is duplicate");
                }
                if(msgMap.containsKey(key)) {
                    msgMap.put(key, String.valueOf(sensorMsg.v));
                    count++;
                }
            }
            // 数据缺失
            if (count != requiredCount) {
                for(Map.Entry<String, String> e : msgMap.entrySet()){
                    if(e.getValue() == null){
                        throw new Exception("key:" + e.getKey() + " is missing");
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
