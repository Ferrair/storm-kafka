package util;

import com.google.inject.Singleton;
import okhttp3.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class AppUtil {

    private static final OkHttpClient instance =
            new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

    public static Response doPost(String url, String json) throws IOException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return instance.newCall(request).execute();
    }

    public static Response doGet(String url, Map<String, Object> headers, Map<String, Object> query) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Request.Builder builder = new Request.Builder();
        HttpUrl.Builder urlBuilder = request.url().newBuilder();
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, Object>> headerIterator = headers.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), (String) e.getValue());
        });

        // 装载请求的参数
        Iterator<Map.Entry<String, Object>> queryIterator = query.entrySet().iterator();
        queryIterator.forEachRemaining(e -> {
            urlBuilder.addQueryParameter(e.getKey(), (String) e.getValue());
        });


        builder.url(urlBuilder.build()).headers(headerBuilder.build());

        return instance.newCall(builder.build()).execute();
    }
}
