package com.mokelab.http;

import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * test for {@link HTTPClientImpl}
 */
@RunWith(AndroidJUnit4.class)
public class HTTPClientImplTest {
    private MyThread thread;
    @Before
    public void initLooper() {
        this.thread = new MyThread();
        this.thread.start();
    }

    @After
    public void stopLooper() {
        thread.looper.quit();
    }

    @Test
    public void send() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Map<String, Object> result = new HashMap<>();

        HTTPClient client = new HTTPClientImpl(new OkHttpClient(), thread.handler);
        client.send(Method.GET, "https://gae-echoserver.appspot.com/test", null, null, new HTTPClient.Callback() {
            @Override
            public void done(HTTPResponse response, HTTPException exception) {
                result.put("response", response);
                result.put("exception", exception);
                latch.countDown();
            }
        });
        latch.await(2000, TimeUnit.MILLISECONDS);
        HTTPResponse response = (HTTPResponse) result.get("response");
        if (response == null) {
            fail("Failed to execute");
            return;
        }
        assertEquals(200, response.status);
        JSONObject json = new JSONObject(response.body);
        assertEquals("/test", json.getString("url"));
        assertEquals("GET", json.getString("method"));
    }

    private static class MyThread extends Thread {
        Handler handler;
        Looper looper;
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            handler = new Handler();
            this.looper = Looper.myLooper();
            Looper.loop();
        }
    }
}