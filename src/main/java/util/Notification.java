package util;

import config.AppConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Notification {

    public enum Type {
        MODEL_FAILURE, // 模型宕机
        KAFKA_FAILURE, // kafka宕机
        CONTROL_FAILURE, // 反向控制宕机
        SAVE_FAILURE, // 数据保存错误

        DATA_ERROR, // kafka传来的数据不合理
        PREDICT_ERROR, // 模型预测的数据不合理
    }

    public static void send(Type type, String msg) {
        Map<String, Object> query = new HashMap<>();
        query.put("type", type);
        query.put("msg", msg);
        try {
            AppUtil.doGet(
                    "",
                    new HashMap<>(),
                    query
            );
        } catch (IOException ignored) {
        }
    }

}
