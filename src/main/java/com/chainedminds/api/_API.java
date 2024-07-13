package com.chainedminds.api;

import com.chainedminds._Config;
import okhttp3.*;
import okio.*;
import org.brotli.dec.BrotliInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class _API {

    private static _API INSTANCE;

    public OkHttpClient okHttp;

    public static synchronized _API instance() {

        if (INSTANCE == null) {

            INSTANCE = new _API();
        }

        return INSTANCE;
    }

    public void init(_Config.OkHTTP config) {

        Proxy proxy = Proxy.NO_PROXY;

        if ("SOCKS".equals(config.OKHTTP_PROXY)) {

            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(config.OKHTTP_PROXY_ADDRESS, config.OKHTTP_PROXY_PORT));
        }

        if ("HTTP".equals(config.OKHTTP_PROXY)) {

            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.OKHTTP_PROXY_ADDRESS, config.OKHTTP_PROXY_PORT));
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(config.OKHTTP_DISPATCHER_MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(config.OKHTTP_DISPATCHER_MAX_REQUEST_PER_HOST);

        ConnectionPool connectionPool = new ConnectionPool(
                config.OKHTTP_CONNECTION_POOL_IDLE_CONNECTIONS,
                config.OKHTTP_CONNECTION_POOL_KEEP_ALIVE_DURATION,
                TimeUnit.SECONDS);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .proxy(proxy)
                .cache(null)
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .followRedirects(config.OKHTTP_FOLLOW_REDIRECTS)
                .callTimeout(config.OKHTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(config.OKHTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(config.OKHTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(config.OKHTTP_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);

        if (config.OKHTTP_NETWORK_INTERCEPTOR != null) {

            builder.addNetworkInterceptor(config.OKHTTP_NETWORK_INTERCEPTOR);
        }

        okHttp = builder.build();
    }

    public void call(Request.Builder builder, boolean asyncMode, ApiCallback apiCallback) {

        if (asyncMode) {

            callAsync(builder, apiCallback);

        } else {

            callSync(builder, apiCallback);
        }
    }

    public void callSync(Request.Builder builder, ApiCallback apiCallback) {

        if (okHttp == null) {

            throw new RuntimeException("OkHTTP is not initialized.");
        }

        try {

            Request request = builder.build();

            try (Response response = okHttp.newCall(request).execute()) {

                int code = response.code();

                okhttp3.Headers baseHeaders = response.headers();

                ResponseBody responseBody = response.body();

                if (responseBody != null) {

                    String responseString = null;

                    if ("br".equals(baseHeaders.get("content-encoding"))) {

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

                        Headers headers = new Headers();

                        baseHeaders.forEach(pair -> headers.add(pair.component1().toLowerCase(), pair.component2()));

                        List<String> cookies = new ArrayList<>();

                        List<String> cookieKeys = new ArrayList<>();
                        List<String> cookieValues = new ArrayList<>();
                        List<List<String>> cookieAttributes = new ArrayList<>();

                        for (_API.HeaderItem header : headers.getAll()) {

                            if (header.key.equals("set-cookie")) {

                                apiCallback.onSetCookie(header.value);

                                cookies.add(header.value);

                                String value = header.value;

                                String cookieValue = value;

                                if (value.contains(";")) {

                                    int semicolonIndex = value.indexOf(";");

                                    cookieValue = value.substring(0, semicolonIndex);
                                    value = value.substring(semicolonIndex + 2);
                                }

                                String[] cookieValueParts = cookieValue.split("=", 2);
                                String cookieValueKey = cookieValueParts[0];
                                String cookieValueValue = cookieValueParts[1];
                                List<String> cookieValueAttributes = new ArrayList<>();

                                for (String cookieValueAttribute : value.split(";")) {

                                    cookieValueAttributes.add(cookieValueAttribute.trim());
                                }

                                apiCallback.onSetCookie(cookieValueKey, cookieValueValue, cookieValueAttributes);

                                cookieKeys.add(cookieValueKey);
                                cookieValues.add(cookieValueValue);
                                cookieAttributes.add(cookieValueAttributes);
                            }
                        }

                        if (!cookies.isEmpty()) {

                            apiCallback.onSetCookies(cookies);
                            apiCallback.onSetCookies(cookieKeys, cookieValues, cookieAttributes);
                        }

                        apiCallback.onResponse(code, responseString);
                        apiCallback.onResponse(code, baseHeaders.toMultimap(), responseString);
                        apiCallback.onResponse(code, headers, responseString);
                    }
                }
            }

        } catch (Exception e) {

            /*HttpUrl url = builder.getUrl$okhttp();

            if (url != null) {

                System.out.println("OKHTTP ERROR : " + url);
            }*/

            if (apiCallback != null) {

                apiCallback.onError(e);
                apiCallback.onError(e.getClass().getSimpleName(), e.getMessage());
                apiCallback.onError(e.getMessage(), e);
            }
        }
    }

    public void callAsync(Request.Builder builder, ApiCallback apiCallback) {

        if (okHttp == null) {

            throw new RuntimeException("OkHTTP is not initialized.");
        }

        Request request = builder.build();

        okHttp.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                /*HttpUrl url = builder.getUrl$okhttp();

                if (url != null) {

                    System.out.println("OKHTTP ERROR : " + url);
                }*/

                if (apiCallback != null) {

                    apiCallback.onError(e);
                    apiCallback.onError(e.getClass().getSimpleName(), e.getMessage());
                    apiCallback.onError(e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                int code = response.code();

                okhttp3.Headers baseHeaders = response.headers();

                try (ResponseBody responseBody = response.body()) {

                    if (responseBody != null) {

                        String responseString = null;

                        if ("br".equals(baseHeaders.get("content-encoding"))) {

                            InputStreamReader inputStream = new InputStreamReader(
                                    new BrotliInputStream(responseBody.byteStream()));

                            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");

                            responseString = scanner.hasNext() ? scanner.next() : "";
                        }

                        if (responseString == null) {

                            responseString = responseBody.string();
                        }

                        if (apiCallback != null) {

                            Headers headers = new Headers();

                            baseHeaders.forEach(pair -> headers.add(pair.component1().toLowerCase(), pair.component2()));

                            List<String> cookies = new ArrayList<>();

                            List<String> cookieKeys = new ArrayList<>();
                            List<String> cookieValues = new ArrayList<>();
                            List<List<String>> cookieAttributes = new ArrayList<>();

                            for (_API.HeaderItem header : headers.getAll()) {

                                if (header.key.equals("set-cookie")) {

                                    apiCallback.onSetCookie(header.value);

                                    cookies.add(header.value);

                                    String value = header.value;

                                    String cookieValue = value;

                                    if (value.contains(";")) {

                                        int semicolonIndex = value.indexOf(";");

                                        cookieValue = value.substring(0, semicolonIndex);
                                        value = value.substring(semicolonIndex + 2);
                                    }

                                    String[] cookieValueParts = cookieValue.split("=", 2);
                                    String cookieValueKey = cookieValueParts[0];
                                    String cookieValueValue = cookieValueParts[1];
                                    List<String> cookieValueAttributes = new ArrayList<>();

                                    for (String cookieValueAttribute : value.split(";")) {

                                        cookieValueAttributes.add(cookieValueAttribute.trim());
                                    }

                                    apiCallback.onSetCookie(cookieValueKey, cookieValueValue, cookieValueAttributes);

                                    cookieKeys.add(cookieValueKey);
                                    cookieValues.add(cookieValueValue);
                                    cookieAttributes.add(cookieValueAttributes);
                                }
                            }

                            if (!cookies.isEmpty()) {

                                apiCallback.onSetCookies(cookies);
                                apiCallback.onSetCookies(cookieKeys, cookieValues, cookieAttributes);
                            }

                            apiCallback.onResponse(code, responseString);
                            apiCallback.onResponse(code, baseHeaders.toMultimap(), responseString);
                            apiCallback.onResponse(code, headers, responseString);
                        }
                    }
                }
            }
        });
    }

    public abstract static class StreamCallback {

        void onClose() {
        }

        void onMessage(String message) {
        }
    }

    public abstract static class ApiCallback {

        public void onError(Exception exception) {

        }

        public void onError(String error, String message) {

        }

        public void onError(String error, Exception exception) {

        }

        //-------------------------------------------------

        public void onResponse(int code, String response) {

        }

        public void onResponse(int code, Map<String, List<String>> headers, String response) {

        }

        public void onResponse(int code, Headers headers, String response) {

        }

        //-------------------------------------------------

        public void onSetCookie(String cookie) {

        }

        public void onSetCookie(String key, String value, List<String> attributes) {

        }

        public void onSetCookies(List<String> cookies) {

        }

        public void onSetCookies(List<String> keys, List<String> values, List<List<String>> attributes) {

        }
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

    public static class Headers {

        private final List<HeaderItem> items = new ArrayList<>();

        public void add(String key, String value) {

            items.add(new HeaderItem(key, value));
        }

        public String get(String key) {

            for (HeaderItem header : items) {

                if (header.key.equals(key)) {

                    return header.value;
                }
            }

            return null;
        }

        public List<HeaderItem> getAll() {

            return items;
        }

        public List<String> getAll(String key) {

            List<String> values = new ArrayList<>();

            for (HeaderItem header : items) {

                if (header.key.equals(key)) {

                    values.add(header.value);
                }
            }

            return values;
        }
    }

    public static class HeaderItem {

        public HeaderItem() {

        }

        public HeaderItem(String key, String value) {

            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }

    public interface ProgressListener {
        void onProgress(long bytesWritten, long contentLength);
    }

    public static class UploadProgressWrapper extends RequestBody {

        private final RequestBody requestBody;
        private final ProgressListener progressListener;
        private BufferedSink bufferedSink;

        public UploadProgressWrapper(RequestBody requestBody, ProgressListener progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        @Override
        public void writeTo(@NotNull BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                bufferedSink = Okio.buffer(sink(sink));
            }
            requestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        }

        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                long bytesWritten = 0L;
                long contentLength = 0L;

                @Override
                public void write(@NotNull Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        contentLength = contentLength();
                    }
                    bytesWritten += byteCount;
                    progressListener.onProgress(bytesWritten, contentLength);
                }
            };
        }
    }
}