package com.chainedminds.api;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

        //Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("vpn.fandoghapps.com", 65001));

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(250);
        dispatcher.setMaxRequestsPerHost(50);

        ConnectionPool connectionPool = new ConnectionPool(50, 1, TimeUnit.MINUTES);

        OK_HTTP_CLIENT = new OkHttpClient.Builder()
                //.addNetworkInterceptor(new LoggingInterceptor())
                //.proxy(proxy)
                .cache(null)
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .callTimeout(3, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
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

                /*Headers headers = response.headers();

                System.out.println(JsonHelper.getString(headers.toMultimap()));*/

                ResponseBody responseBody = response.body();

                if (responseBody != null) {

                    String responseString = responseBody.string();

                    responseBody.close();

                    if (apiCallback != null) {

                        apiCallback.onResponse(responseString);
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

                /*Headers headers = response.headers();

                System.out.println(JsonHelper.getString(headers.toMultimap()));*/

                try (ResponseBody responseBody = response.body()) {

                    if (responseBody != null) {

                        String responseString = responseBody.string();

                        if (apiCallback != null) {

                            apiCallback.onResponse(responseString);
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

        void onResponse(String response);
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