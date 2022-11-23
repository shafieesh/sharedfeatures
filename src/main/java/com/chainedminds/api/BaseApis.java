package com.chainedminds.api;

import com.chainedminds.BaseConfig;
import okhttp3.*;
import org.brotli.dec.BrotliInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BaseApis {

    public static OkHttpClient OK_HTTP_CLIENT;

    static {

        buildHttpClient();
    }

    private static synchronized void buildHttpClient() {

        if (OK_HTTP_CLIENT != null) {

            return;
        }

        Proxy proxy = Proxy.NO_PROXY;

        if ("SOCKS".equals(BaseConfig.OKHTTP_PROXY)) {

            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(BaseConfig.OKHTTP_PROXY_ADDRESS, BaseConfig.OKHTTP_PROXY_PORT));
        }

        if ("HTTP".equals(BaseConfig.OKHTTP_PROXY)) {

            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(BaseConfig.OKHTTP_PROXY_ADDRESS, BaseConfig.OKHTTP_PROXY_PORT));
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(BaseConfig.OKHTTP_DISPATCHER_MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(BaseConfig.OKHTTP_DISPATCHER_MAX_REQUEST_PER_HOST);

        ConnectionPool connectionPool = new ConnectionPool(
                BaseConfig.OKHTTP_CONNECTION_POOL_IDLE_CONNECTIONS,
                BaseConfig.OKHTTP_CONNECTION_POOL_KEEP_ALIVE_DURATION,
                TimeUnit.SECONDS);

        OK_HTTP_CLIENT = new OkHttpClient.Builder()
                //.addNetworkInterceptor(new LoggingInterceptor())
                .proxy(proxy)
                .cache(null)
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .callTimeout(BaseConfig.OKHTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(BaseConfig.OKHTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    public static void call(Request.Builder builder, boolean asyncMode, ApiCallback apiCallback) {

        if (asyncMode) {

            callAsync(builder, apiCallback);

        } else {

            callSync(builder, apiCallback);
        }
    }

    public static void callSync(Request.Builder builder, ApiCallback apiCallback) {

        try {

            Request request = builder.build();

            try (Response response = BaseApis.OK_HTTP_CLIENT.newCall(request).execute()) {

                Headers headers = response.headers();

                ResponseBody responseBody = response.body();

                //System.out.println(JsonHelper.getString(headers.toMultimap()));

                if (responseBody != null) {

                    String responseString = null;

                    if ("br".equals(headers.get("content-encoding"))) {

                        InputStreamReader inputStream = new InputStreamReader(
                                new BrotliInputStream(responseBody.byteStream()));

                        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");

                        responseString = scanner.hasNext() ? scanner.next() : "";
                    }

                    if (responseString == null) {

                        responseString = responseBody.string();
                    }

                    responseBody.close();

                    if (apiCallback != null) {

                        apiCallback.onResponse(headers.toMultimap(), responseString);
                    }
                }
            }

        } catch (Exception e) {

            /*HttpUrl url = builder.getUrl$okhttp();

            if (url != null) {

                System.out.println("OKHTTP ERROR : " + url);
            }

            e.printStackTrace();*/

            if (apiCallback != null) {

                apiCallback.onError(e.getMessage());
            }
        }
    }

    public static void callAsync(Request.Builder builder, ApiCallback apiCallback) {

        Request request = builder.build();

        BaseApis.OK_HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                /*HttpUrl url = builder.getUrl$okhttp();

                if (url != null) {

                    System.out.println("OKHTTP ERROR : " + url);
                }

                e.printStackTrace();*/

                if (apiCallback != null) {

                    apiCallback.onError(e.getMessage());
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                Headers headers = response.headers();

                //System.out.println(JsonHelper.getString(headers.toMultimap()));

                try (ResponseBody responseBody = response.body()) {

                    if (responseBody != null) {

                        String responseString = null;

                        if ("br".equals(headers.get("content-encoding"))) {

                            InputStreamReader inputStream = new InputStreamReader(
                                    new BrotliInputStream(responseBody.byteStream()));

                            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");

                            responseString = scanner.hasNext() ? scanner.next() : "";
                        }

                        if (responseString == null) {

                            responseString = responseBody.string();
                        }

                        if (apiCallback != null) {

                            apiCallback.onResponse(headers.toMultimap(), responseString);
                        }
                    }
                }
            }
        });
    }

    public interface StreamCallback {

        void onClose();

        void onMessage(String message);
    }

    public interface ApiCallback {

        void onError(String error);

        void onResponse(Map<String, List<String>> headers, String response);
    }

    static class LoggingInterceptor implements Interceptor {

        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();

            long t1 = System.nanoTime();

            System.out.printf("Sending request %s on %s%n%s%n",
                    request.url(), chain.connection(), request.headers());

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();

            System.out.printf("Received response for %s in %.1fms%n%s%n",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers());

            return response;
        }
    }
}