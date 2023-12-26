package com.chainedminds.api;

import com.chainedminds._Config;
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

public class _Api {

    public static OkHttpClient OK_HTTP_CLIENT;

    static {

        buildHttpClient();
    }

    private static synchronized void buildHttpClient() {

        if (OK_HTTP_CLIENT != null) {

            return;
        }

        Proxy proxy = Proxy.NO_PROXY;

        if ("SOCKS".equals(_Config.OKHTTP_PROXY)) {

            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(_Config.OKHTTP_PROXY_ADDRESS, _Config.OKHTTP_PROXY_PORT));
        }

        if ("HTTP".equals(_Config.OKHTTP_PROXY)) {

            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_Config.OKHTTP_PROXY_ADDRESS, _Config.OKHTTP_PROXY_PORT));
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(_Config.OKHTTP_DISPATCHER_MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(_Config.OKHTTP_DISPATCHER_MAX_REQUEST_PER_HOST);

        ConnectionPool connectionPool = new ConnectionPool(
                _Config.OKHTTP_CONNECTION_POOL_IDLE_CONNECTIONS,
                _Config.OKHTTP_CONNECTION_POOL_KEEP_ALIVE_DURATION,
                TimeUnit.SECONDS);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .proxy(proxy)
                .cache(null)
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .followRedirects(_Config.OKHTTP_FOLLOW_REDIRECTS)
                .callTimeout(_Config.OKHTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(_Config.OKHTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(_Config.OKHTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(_Config.OKHTTP_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);

        if (_Config.OKHTTP_NETWORK_INTERCEPTOR != null) {

            builder.addNetworkInterceptor(_Config.OKHTTP_NETWORK_INTERCEPTOR);
        }

        OK_HTTP_CLIENT = builder.build();
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

            try (Response response = _Api.OK_HTTP_CLIENT.newCall(request).execute()) {

                int code = response.code();

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

                        apiCallback.onResponse(code, headers.toMultimap(), responseString);
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

        _Api.OK_HTTP_CLIENT.newCall(request).enqueue(new Callback() {
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

                int code = response.code();

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

                            apiCallback.onResponse(code, headers.toMultimap(), responseString);
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

        void onResponse(int code, Map<String, List<String>> headers, String response);
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