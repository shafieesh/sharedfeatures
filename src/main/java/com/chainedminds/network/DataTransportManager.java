package com.chainedminds.network;

import com.chainedminds.*;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.ClientData;
import com.chainedminds.utilities.Log;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.json.JsonHelper;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataTransportManager extends Thread {

    private static final String TAG = DataTransportManager.class.getSimpleName();

    private static final int INSECURE_PREFIX_DATA_LENGTH = 10;
    private static ServerSocket webTransportPipe;
    private static ServerSocket mainPipe;
    private static Thread webTransportThread;
    private static Thread mainPipeThread;
    private static boolean webTransportWorking;
    private static boolean mainPipeWorking;
    private static int openConnections = 0;
    private static int newConnections = 0;
    private static final Map<Long, Connection> activeConnections = new HashMap<>();
    private static final Map<Long, Long> lastConnectionTimes = new HashMap<>();
    private static final Map<Long, Integer> connectionHandles = new HashMap<>();

    private static final Map<Integer, Integer> requests = new HashMap<>();

    private static final ExecutorService MAIN_PIPE_EXECUTOR = Executors.newCachedThreadPool();
    private static final ExecutorService WEB_PIPE_EXECUTOR = Executors.newCachedThreadPool();

    private DataTransportManager() {

        System.out.println();
        System.out.println("---------------------------");
        System.out.println("--> Opening Transport Pipes");
        System.out.println();

        startNotWorkingTransports();

        System.out.println("---------------------------");
    }

    private static void saveRequest(int request) {

        requests.put(request, requests.getOrDefault(request, 0));
    }

    public static Map<Integer, Integer> getPopularRequests() {

        Map<Integer, Integer> popularRequests = Utilities.sortByValue(requests);

        requests.clear();

        return popularRequests;
    }

    public static int getOpenConnections() {
        return openConnections;
    }

    public static int getNewConnections() {
        int a = newConnections;
        newConnections = 0;
        return a;
    }

    private static String httpGetData(String urlString, Map<String, String> headers, String requestMethod, String payload) {

        String receivedData = null;

        try {

            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("213.32.14.69", 65000));

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            if (headers != null) {

                for (String key : headers.keySet()) {

                    connection.setRequestProperty(key, headers.get(key));
                }
            }

            if (requestMethod != null) {
                connection.setRequestMethod(requestMethod);
                connection.setDoOutput(true);
            }

            if (payload != null) {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(payload.getBytes());
                outputStream.close();
            }

            BufferedReader bufferedReader;

            if (connection.getResponseCode() != 200) {
                //throw new IOException(connection.getResponseMessage());

                bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

            } else {

                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            StringBuilder stringBuilder = new StringBuilder();

            String line;

            while ((line = bufferedReader.readLine()) != null) {

                stringBuilder.append(line);
            }

            bufferedReader.close();
            connection.disconnect();
            receivedData = stringBuilder.toString();

        } catch (Exception e) {

            e.printStackTrace();

            Log.error("DataTransportManager", e, urlString + "\n" + payload);
        }
        return receivedData;
    }

    public static String httpGet(String urlString) {
        return httpGetData(urlString, null, null, null);
    }

    public static String httpGet(String urlString, Map<String, String> headers) {
        return httpGetData(urlString, headers, null, null);
    }

    public static String httpPost(String urlString, String payload) {
        return httpGetData(urlString, null, "POST", payload);
    }

    public static String httpPost(String urlString, Map<String, String> headers, String payload) {
        return httpGetData(urlString, headers, "POST", payload);
    }

    public static String httpDelete(String urlString) {
        return httpGetData(urlString, null, "DELETE", null);
    }

    public static byte[] httpDownload(String urlString) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new URL(urlString).openStream());
            final byte[] data = new byte[1024];
            int count;
            while ((count = inputStream.read(data, 0, 1024)) != -1) {
                byteArrayOutputStream.write(data, 0, count);
            }
            inputStream.close();
        } catch (Exception e) {

            Log.error("DataTransportManager", e);
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String getData(String host, int port, Object sendingData) {

        String receivedMessage = null;

        Socket socket = new Socket();

        try {

            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(30000);

            String sendingMessage;

            if (sendingData instanceof String) {

                sendingMessage = (String) sendingData;

            } else {

                sendingMessage = JsonHelper.getString(sendingData);
            }

            socketWriteInsecure(socket, sendingMessage);

            receivedMessage = socketReadInsecure(socket);

        } catch (Exception e) {

            //e.printStackTrace();

            System.out.println(e.getMessage());

        } finally {

            Utilities.tryAndIgnore(socket::close);
        }

        return receivedMessage;
    }

    public static String transport(String host, int port, String sendingData) {

        String receivedMessage = null;

        try {

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(30000);
            //socket.setSoLinger(true, 1);

            String sendingMessage;

            if (sendingData instanceof String) {

                sendingMessage = sendingData;

            } else {

                sendingMessage = JsonHelper.getString(sendingData);
            }

            socketWriteInsecure(socket, sendingMessage);

            receivedMessage = socketReadInsecure(socket);

            socket.close();

        } catch (Exception e) {

            //e.printStackTrace();
        }

        return receivedMessage;
    }

    public static String socketReadInsecure(Socket socket) throws IOException {

        InputStream inputStream = socket.getInputStream();

        int availableBytes = INSECURE_PREFIX_DATA_LENGTH;

        byte[] buffer = new byte[availableBytes];
        int totalBytesRead = 0;
        int bytesRead = 0;

        while (bytesRead != -1 && totalBytesRead < availableBytes) {

            bytesRead = inputStream.read(buffer, totalBytesRead, availableBytes - totalBytesRead);

            totalBytesRead += bytesRead;
        }

        if (bytesRead == -1) {

            throw new IOException("EOF");
        }

        availableBytes = 0;

        for (int i = buffer.length - 1, j = 1; i >= 0; i--, j *= 10) {

            availableBytes += buffer[i] * j;
        }

        if (availableBytes < 0 || availableBytes > 1024 * 1024 * 10) {

            throw new IOException("DamagedData");
        }

        buffer = new byte[availableBytes];
        totalBytesRead = 0;
        bytesRead = 0;

        while (bytesRead != -1 && totalBytesRead < availableBytes) {

            bytesRead = inputStream.read(buffer, totalBytesRead, availableBytes - totalBytesRead);

            totalBytesRead += bytesRead;
        }

        return new String(buffer);
    }

    public static byte[] socketReadBytes(Socket socket) throws IOException {

        InputStream inputStream = socket.getInputStream();

        int availableBytes = INSECURE_PREFIX_DATA_LENGTH;

        byte[] buffer = new byte[availableBytes];
        int totalBytesRead = 0;
        int bytesRead = 0;

        while (bytesRead != -1 && totalBytesRead < availableBytes) {

            bytesRead = inputStream.read(buffer, totalBytesRead, availableBytes - totalBytesRead);

            totalBytesRead += bytesRead;
        }

        if (bytesRead == -1) {

            throw new IOException("EOF");
        }

        availableBytes = 0;

        for (int i = buffer.length - 1, j = 1; i >= 0; i--, j *= 10) {

            availableBytes += buffer[i] * j;
        }

        if (availableBytes < 0 || availableBytes > 1024 * 1024 * 10) {

            throw new IOException("DamagedData");
        }

        buffer = new byte[availableBytes];
        totalBytesRead = 0;
        bytesRead = 0;

        while (bytesRead != -1 && totalBytesRead < availableBytes) {

            bytesRead = inputStream.read(buffer, totalBytesRead, availableBytes - totalBytesRead);

            totalBytesRead += bytesRead;
        }

        return buffer;
    }

    public static void socketWriteInsecure(Socket socket, String sendingMessage) throws IOException {

        OutputStream outputStream = socket.getOutputStream();

        byte[] payload = sendingMessage.getBytes(StandardCharsets.UTF_8);

        byte[] buffer = new byte[10];

        int messageLength = payload.length;

        for (int i = buffer.length - 1, j = 1; i >= 0; i--, j *= 10) {

            buffer[i] = (byte) ((messageLength / j) % 10);
        }

        outputStream.write(buffer);
        outputStream.write(payload);
        outputStream.flush();
    }

    public static void socketWriteBytes(Socket socket, byte[] data) throws IOException {

        OutputStream outputStream = socket.getOutputStream();

        byte[] buffer = new byte[10];

        int messageLength = data.length;

        for (int i = buffer.length - 1, j = 1; i >= 0; i--, j *= 10) {

            buffer[i] = (byte) ((messageLength / j) % 10);
        }

        outputStream.write(buffer);
        outputStream.write(data);
        outputStream.flush();
    }

    private void startNotWorkingTransports() {

        if (!mainPipeWorking && BaseMonitor.isAppRunning()) {

            startInsecureTransport();
        }

        if (!webTransportWorking && BaseMonitor.isAppRunning()) {

            startWebTransport();
        }
    }

    @Override
    public void run() {

        while (BaseMonitor.isAppRunning()) {

            try {

                if (mainPipeThread == null || !mainPipeThread.isAlive()) {

                    mainPipeWorking = false;

                    System.out.println("--> Reopening Insecure Transport Pipe");
                }

                if (webTransportThread == null || !webTransportThread.isAlive()) {

                    webTransportWorking = false;

                    System.out.println("--> Reopening Web Transport Pipe");
                }

                startNotWorkingTransports();

                Utilities.sleep(10000);

            } catch (Exception e) {

                Log.error("DataTransportManager", e);
            }
        }

        closeAllSockets();
    }

    private void closeAllSockets() {

        try {

            mainPipe.close();

        } catch (Exception ignored) {
        }

        try {

            webTransportPipe.close();

        } catch (Exception ignored) {
        }
    }

    private <Data extends BaseData> void startWebTransport() {

        try {

            webTransportPipe = new ServerSocket(BaseConfig.SERVER_PORT_TELNET);

            while (BaseMonitor.isAppRunning()) {

                try {

                    webTransportWorking = true;

                    WEB_PIPE_EXECUTOR.execute(new WebTransportThread<Data>(webTransportPipe.accept()));

                } catch (Exception e) {

                    if (BaseMonitor.isAppRunning()) {

                        Log.error("WebTransport", e);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("--- Network-> WebTransport failed to start");

            Log.error("DataTransportManager", e);
        }
    }

    private void startInsecureTransport() {

        try {

            mainPipe = new ServerSocket(BaseConfig.SERVER_PORT_MAIN);

            while (BaseMonitor.isAppRunning()) {

                try {

                    mainPipeWorking = true;

                    MAIN_PIPE_EXECUTOR.execute(new MainPipeThread(mainPipe.accept()));

                } catch (Exception e) {

                    if (BaseMonitor.isAppRunning()) {

                        Log.error("Network-MainPipe", e);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("--- Network-> MainPipe failed to start");

            Log.error(TAG, e);
        }
    }

    public static void startTestTransport() {

        try {

            mainPipe = new ServerSocket(BaseConfig.SERVER_PORT_MAIN);

            while (BaseMonitor.isAppRunning()) {

                try {

                    mainPipeWorking = true;

                    MAIN_PIPE_EXECUTOR.execute(new MainPipeThread(mainPipe.accept()));

                } catch (Exception e) {

                    if (BaseMonitor.isAppRunning()) {

                        Log.error("Network-MainPipe", e);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("--- Network-> MainPipe failed to start");

            Log.error(TAG, e);
        }
    }

    public static void onDisconnect() {

        openConnections--;
    }

    static class WebTransportThread<Data extends BaseData> implements Runnable {

        Socket socket;

        private InputStream inputStream;
        private OutputStream outputStream;

        private BufferedReader streamReader;
        private BufferedWriter streamWriter;

        WebTransportThread(Socket socket) {

            newConnections++;
            openConnections++;
            this.socket = socket;

            try {

                inputStream = this.socket.getInputStream();
                outputStream = this.socket.getOutputStream();

            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {

                socket.setSoTimeout(BaseConfig.SOCKET_TIME_OUT_API_PIPE);

                streamWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                streamWriter.flush();
                streamReader = new BufferedReader(new InputStreamReader(inputStream));

                String receivedMessage = streamReader.readLine();

                Data receivedData = (Data) JsonHelper.getObject(receivedMessage, BaseClasses.getInstance().dataClass);

                if (receivedData != null) {

                    saveRequest(receivedData.request);

                    if (receivedData.client == null) {

                        receivedData.client = new ClientData();
                    }

                    if (receivedData.client.address == null) {

                        InetAddress inetAddress = socket.getInetAddress();

                        receivedData.client.address = inetAddress.getHostAddress();
                    }

                    String sendingMessage = (String) BaseResources.getInstance().requestManager.handleRequest(receivedData, null);

                    if (sendingMessage != null) {

                        streamWriter.write(sendingMessage);
                    }

                } else {

                    BaseData sendingData = new BaseData();
                    sendingData.response = BaseCodes.RESPONSE_NOK;
                    sendingData.message = "The request was not ok";

                    String sendingMessage = JsonHelper.getString(sendingData);

                    streamWriter.write(sendingMessage);
                }

                streamWriter.flush();

                streamWriter.close();

                streamReader.close();

            } catch (Exception e) {

                Log.error("WebTransportThread", e);
            }

            try {
                socket.close();
            } catch (Exception ignore) {
            }

            socket = null;
            openConnections--;
        }
    }

    static class MainPipeThread<Data extends BaseData> implements Runnable {

        final Socket socket;

        MainPipeThread(Socket socket) {

            newConnections++;
            openConnections++;
            this.socket = socket;
        }

        @Override
        public void run() {

            try {

                socket.setTcpNoDelay(true);
                socket.setSoTimeout(BaseConfig.SOCKET_TIME_OUT_MAIN_PIPE);

                while (true) {

                    String receivedMessage = DataTransportManager.socketReadInsecure(socket);

                    Data receivedData = (Data) JsonHelper.getObject(receivedMessage, BaseClasses.getInstance().dataClass);

                    if (receivedData != null) {

                        saveRequest(receivedData.request);

                        if (receivedData.client == null) {

                            receivedData.client = new ClientData();
                        }

                        if (receivedData.client.address == null) {

                            InetAddress inetAddress = socket.getInetAddress();

                            receivedData.client.address = inetAddress.getHostAddress();
                        }

                        String sendingMessage = (String) BaseResources.getInstance().requestManager.handleRequest(receivedData, socket);

                        if (sendingMessage != null) {

                            DataTransportManager.socketWriteInsecure(socket, sendingMessage);
                        }

                    } else {

                        BaseData sendingData = new BaseData();
                        sendingData.response = BaseCodes.RESPONSE_NOK;
                        sendingData.message = "The request was not ok";

                        String sendingMessage = JsonHelper.getString(sendingData);

                        DataTransportManager.socketWriteInsecure(socket, sendingMessage);
                    }
                }

            } catch (Exception e) {

                //Log.error("MainPipeThread", e);
            }

            try {
                //socket.close();
            } catch (Exception ignore) {

            }

            //socket = null;
            openConnections--;
        }
    }

    static class TestPipeThread implements Runnable {

        final Socket socket;

        TestPipeThread(Socket socket) {

            newConnections++;
            openConnections++;
            this.socket = socket;
        }

        @Override
        public void run() {

            try {

                socket.setTcpNoDelay(true);
                socket.setSoTimeout(BaseConfig.SOCKET_TIME_OUT_MAIN_PIPE);

                for (; ; ) {

                    String receivedMessage = DataTransportManager.socketReadInsecure(socket);

                    //System.out.println(receivedMessage);

                    String sendingMessage = "Server time: " + System.currentTimeMillis();

                    DataTransportManager.socketWriteInsecure(socket, sendingMessage);
                }

            } catch (Exception e) {

                //Log.error("MainPipeThread", e);
            }

            try {
                //socket.close();
            } catch (Exception ignore) {

            }

            //socket = null;
            openConnections--;
        }
    }
}