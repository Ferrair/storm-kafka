package msg;

import gherkin.deps.com.google.gson.annotations.Expose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OriginalMsg implements Serializable {

    public class SensorMsg {
        Object id;
        Object v;
        // Object q;
        // Object t;
    }

    @Expose
    private long timestamp;

    public List<SensorMsg> getValues() {
        return values;
    }

    @Expose
    private List<SensorMsg> values;


    /**
     * 记录所有PLC点位的对应关系
     */
    private static final Map<String, String> mapping = new HashMap<>();

    /**
     * 记录设备状态的对应关系
     */
    private static final Map<Integer, String> deviceStatusMapping = new HashMap<>();

    /**
     * 记录数值点位的数量 (5H.5H开头的)
     */
    private static int requiredCount;

    private static final Logger logger = LoggerFactory.getLogger(OriginalMsg.class);

    static {
        mapping.put("batch", "6032.6032.LD5_YT603_2B_YS2ROASTBATCHNO");
        mapping.put("brand", "6032.6032.LD5_YT603_2B_YS2ROASTBRAND");
        mapping.put("deviceStatus1", "5H.5H.LD5_KL2226_PHASE1");
        mapping.put("deviceStatus2", "5H.5H.LD5_KL2226_PHASE2");
        // mapping.put("siroxWorkStatus", "5H.5H.LD5_KL2226_SiroxWorkStatus");
        mapping.put("flowAcc", "5H.5H.LD5_CK2222_TbcLeafFlowSH");
        mapping.put("humidOut", "5H.5H.LD5_KL2226_ZF2LeafMois");
        mapping.put("humidIn", "5H.5H.LD5_KL2226_InputMoisture");
        mapping.put("humidSetting", "5H.5H.LD5_KL2226_TT1LastMoisSP");
        mapping.put("windSpeed", "5H.5H.LD5_KL2226_ProcAirSpeedPV");
        mapping.put("tempSetting1", "5H.5H.LD5_KL2226_BucketTemp1SP");
        mapping.put("tempActual1", "5H.5H.LD5_KL2226_BucketTemp1PV");
        mapping.put("tempSetting2", "5H.5H.LD5_KL2226_BucketTemp2SP");
        mapping.put("tempActual2", "5H.5H.LD5_KL2226_BucketTemp2PV");
        mapping.put("press", "5H.5H.LD5_KL2226_BacCoverPrePV");

        deviceStatusMapping.put(8, "准备");
        deviceStatusMapping.put(16, "启动");
        deviceStatusMapping.put(32, "生产");
        deviceStatusMapping.put(64, "收尾");

        mapping.values().forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                requiredCount = s.startsWith("5H.5H") ? requiredCount + 1 : requiredCount;
            }
        });

    }

    private static String getKey(String value) {
        String key = "";
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (value.equals(entry.getValue())) {
                key = entry.getKey();
            }
        }
        return key;
    }


    public static boolean isInProductMode(int deviceStatus) {
        return deviceStatusMapping.containsKey(deviceStatus);
    }


    public OriginalMsg(long timestamp, List<SensorMsg> values) {
        this.timestamp = timestamp;
        this.values = values;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPLCSize() {
        return values == null ? 0 : values.size();
    }

    public double getFlowAcc() {
        try {
            if (values.size() == 0) {
                return -1d;
            }
            for (SensorMsg sensorMsg : values) {
                if (sensorMsg.id instanceof String && sensorMsg.id.equals(mapping.get("flowAcc"))) {
                    Double v;
                    try {
                        v = Double.parseDouble((String) sensorMsg.v);
                    } catch (Exception e) {
                        v = (Double) sensorMsg.v;
                    }
                    return v;
                }
            }
            return -1d;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return -1d;
        }
    }

    public boolean isBatchStart() {
        return getFlowAcc() == 0;
    }

    public int getDeviceStatus() {
        if (values.size() == 0) {
            return 1;
        }
        try {
            double deviceStatus1 = 1d, deviceStatus2 = 0d;
            for (SensorMsg sensorMsg : values) {
                if (sensorMsg.id instanceof String && sensorMsg.id.equals(mapping.get("deviceStatus1"))) {
                    try {
                        deviceStatus1 = Double.parseDouble((String) sensorMsg.v);
                    } catch (Exception e) {
                        deviceStatus1 = (Double) sensorMsg.v;
                    }
                }
                if (sensorMsg.id instanceof String && sensorMsg.id.equals(mapping.get("deviceStatus2"))) {
                    try {
                        deviceStatus2 = Double.parseDouble((String) sensorMsg.v);
                    } catch (Exception e) {
                        deviceStatus2 = (Double) sensorMsg.v;
                    }
                }
            }
            return (int) (deviceStatus1 + deviceStatus2 * 256);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 1;
        }

    }

    public String getBrand() {
        try {
            if (values.size() == 0) {
                return "";
            }
            for (SensorMsg sensorMsg : values) {
                if (sensorMsg.id instanceof String && sensorMsg.id.equals(mapping.get("brand"))) {
                    return (String) sensorMsg.v;
                }
            }
            return "";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    public String getBatch() {
        try {
            if (values.size() == 0) {
                return "";
            }
            for (SensorMsg sensorMsg : values) {
                if (sensorMsg.id instanceof String && sensorMsg.id.equals(mapping.get("batch"))) {
                    return (String) sensorMsg.v;
                }
            }
            return "";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    /**
     * 从点位中得到数据
     * 1. 点位信息缺失 -> 返回空数组
     * 2. 点位信息重复 -> 返回空数组
     */
    public Double[] generate() {
        if (values.size() == 0) {
            return new Double[]{};
        }
        Map<String, Double> result = new HashMap<>();
        Double[] msg = new Double[]{};
        try {
            int count = 0;
            for (SensorMsg sensorMsg : values) {
                String key = getKey((String) sensorMsg.id);
                // 数据中可能存在2个相同的key (brand 和 batch)
                if (sensorMsg.id instanceof String && mapping.containsValue(sensorMsg.id) && !result.containsKey(key)) {
                    if (((String) sensorMsg.id).startsWith("5H.5H")) {
                        count++;
                        try {
                            result.put(key, Double.parseDouble((String) sensorMsg.v));
                        } catch (Exception e) {
                            result.put(key, (Double) sensorMsg.v);
                        }
                    }
                }
            }
            if (count != requiredCount) {
                logger.error("Time in " + getTimestamp() + " is missing or duplicate value: current=" + count + " while required=" + requiredCount);
                return new Double[]{};
            }
            msg = new Double[]{
                    result.get("humidOut") - result.get("humidSetting"),
                    result.get("windSpeed"),
                    result.get("humidOut"),
                    result.get("humidIn"),
                    result.get("press"),
                    result.get("tempActual1"),
                    result.get("tempSetting1"),
                    result.get("tempActual2"),
                    result.get("tempSetting2"),
            };
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return msg;
    }

    @Override
    public String toString() {
        return getTimestamp() + " brand:" +
                getBrand() + " plc_size:" +
                getPLCSize() + " batch:" +
                getBatch() + " device_status:" +
                getDeviceStatus() + " flow:" +
                getFlowAcc() + " features:" +
                Arrays.toString(generate());
    }
}
