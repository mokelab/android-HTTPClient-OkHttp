package com.mokelab.http;

import android.os.Handler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Implementation
 */
public class HTTPClientImpl implements HTTPClient {
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=utf-8";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final OkHttpClient client;
    private final Handler handler;

    public HTTPClientImpl(OkHttpClient client, Handler handler) {
        this.client = client;
        this.handler = handler;
    }

    @Override
    public void send(Method method, String url, Header header, String body, final Callback callback) {
        Request.Builder builder = new Request.Builder();
        switch (method) {
        case GET:
            builder.get();
            break;
        case POST:
            builder.post(RequestBody.create(MediaType.parse(getContentType(header)), body));
            break;
        case PUT:
            builder.put(RequestBody.create(MediaType.parse(getContentType(header)), body));
            break;
        case DELETE:
            builder.delete();
            break;
        }
        builder.url(url);
        setHeaders(builder, header);
        Request request = builder.build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final HTTPResponse httpResponse = new HTTPResponse();
                httpResponse.status = response.code();
                if (httpResponse.status == 204) {
                    httpResponse.body = "";
                } else {
                    httpResponse.body = response.body().string();
                }
                httpResponse.header = new HeaderImpl(response.headers());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.done(httpResponse, null);
                    }
                });
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.done(null, new HTTPException(e));
                    }
                });
            }
        });
    }

    private String getContentType(Header header) {
        if (header == null) {
            return CONTENT_TYPE_TEXT_PLAIN;
        }
        String type = header.get(HEADER_CONTENT_TYPE);
        if (type == null) {
            return CONTENT_TYPE_TEXT_PLAIN;
        }
        return type;
    }

    private void setHeaders(Request.Builder builder, Header header) {
        if (header == null) {
            return;
        }
        Set<Map.Entry<String, String>> entries = header.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }
}
