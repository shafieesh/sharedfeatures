package com.chainedminds.network;

import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.json.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataTransport {

    private static final String TAG = DataTransport.class.getSimpleName();

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

                sendingMessage = Json.getString(sendingData);
            }

            socketWriteInsecure(socket, sendingMessage);

            receivedMessage = socketReadInsecure(socket);

        } catch (Exception e) {

            e.printStackTrace();

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

                sendingMessage = Json.getString(sendingData);
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

    /*private void closeAllSockets() {

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

                        BaseLogs.error("WebTransport", e);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("--- Network-> WebTransport failed to start");

            BaseLogs.error("DataTransportManager", e);
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

                        BaseLogs.error("Network-MainPipe", e);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("--- Network-> MainPipe failed to start");

            BaseLogs.error(TAG, e);
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

                        BaseLogs.error("Network-MainPipe", e);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("--- Network-> MainPipe failed to start");

            BaseLogs.error(TAG, e);
        }
    }

    static class WebTransportThread<Data extends BaseData<?>> implements Runnable {

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

                BaseLogs.error("WebTransportThread", e);
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
    }*/
}