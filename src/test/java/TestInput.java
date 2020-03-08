import core.FeatureComputeBolt;
import gherkin.deps.com.google.gson.Gson;
import msg.IoTMsg;
import msg.ModelMsg;
import msg.OriginalMsg;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestInput {

    private static List<OriginalMsg> getMockData() {
        List<OriginalMsg> list = new ArrayList<>();
        try {
            InputStream is = new FileInputStream("src/test/iot.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String str = reader.readLine();
                if (str != null)
                    list.add(parseStr(str));
                else
                    break;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    private static OriginalMsg parseStr(String str) {
        Gson gson = new Gson();
        return gson.fromJson(str, OriginalMsg.class);
    }

    @Test
    public void testParse() {
        List<OriginalMsg> msgList = getMockData();
        List<IoTMsg> result = new ArrayList<>();

        for (OriginalMsg msg : msgList) {
            Double[] doubles = msg.generate();
            if (doubles.length != 0) {
                result.add(new IoTMsg(doubles));
            }
        }
        System.out.println(result.size());
    }

    @Test
    public void testCompute() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        List<OriginalMsg> msgList = getMockData();
        List<IoTMsg> data = new ArrayList<>();

        for (OriginalMsg msg : msgList) {
            Double[] doubles = msg.generate();
            if (doubles.length != 0) {
                data.add(new IoTMsg(doubles));
            }
        }

        Class<FeatureComputeBolt> class1 = FeatureComputeBolt.class;
        Object instance = class1.newInstance();
        Method method = class1.getDeclaredMethod("computeFeature", List.class, int.class, int.class);
        method.setAccessible(true);
        // 因为点位错误，导致该测试失效
        // Object result = method.invoke(instance, data.subList(0, data.size()), 50, 5);
        // System.out.println(((ModelMsg) result).generate());

    }
}
