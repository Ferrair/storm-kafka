import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import util.AppUtil;

import java.io.IOException;
import java.util.*;

public class TestAPI {

    @Test
    public void testControl() {
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> region1 = new HashMap<>();
        region1.put("Address", "5H.5H.K1_Z7TT1VALUE_1");
        region1.put("Value", 200);
        region1.put("DataType", "float");

        Map<String, Object> region2 = new HashMap<>();
        region2.put("Address", "5H.5H.K1_Z7TT1VALUE_4");
        region2.put("Value", 200);
        region2.put("DataType", "float");

        params.add(region1);
        params.add(region2);

        Response response = null;
        try {
            response = AppUtil.doPost("http://10.114.147.6/api/PLC/SetPLC", String.valueOf(new JSONArray(params)));
            JSONArray json = new JSONArray(Objects.requireNonNull(response.body()).string());
            if (!json.getJSONObject(0).getBoolean("IsSetSucessful") || !json.getJSONObject(1).getBoolean("IsSetSucessful")) {
                System.out.println("PLC set false, please try again");
            } else {
                System.out.println("PLC set OK");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
