package com.chainedminds.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketPool {

    private final Pool pool;

    public SocketPool(String address, int port, int poolSize) {

        this.pool = new Pool(address, port, poolSize);

        Pool.Checker checker = connection -> {

            try {

                byte[] sendingMessage = ("{\"request\":-1,\"message\":\"Pool Position " + connection.position + "\"}").getBytes(StandardCharsets.UTF_8);

                Transfer.writeUnsafe(connection, sendingMessage);

                byte[] receivedMessage = Transfer.readUnsafe(connection);

                //System.out.println(new String(receivedMessage));

                return true;

            } catch (Exception e) {

                //e.printStackTrace();

                return false;
            }
        };

        this.pool.setChecker(checker);

        Task.add(Task.Data.build()
                .setName("Ping Connections")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 0, 10)
                .onEachCycle(pool::pingAll)
                .schedule());
    }

    public Socket connect() {

        try {

            return pool.get();

        } catch (Exception e) {

            //_Log.error(TAG, e);
        }

        return null;
    }

    public boolean close(Socket socket) {

        boolean wasSuccessful = false;

        if (socket != null) {

            Connection connection = (Connection) socket;

            pool.free(connection);

            wasSuccessful = true;
        }

        return wasSuccessful;
    }

    public boolean remove(Socket socket) {

        boolean wasSuccessful = false;

        if (socket != null) {

            Connection connection = (Connection) socket;

            pool.remove(connection.position, connection);

            wasSuccessful = true;
        }

        return wasSuccessful;
    }

    //----------------------------------------------------------------------------

    public static byte[] transfer(SocketPool socketPool, byte[] request) {

        Socket socket = socketPool.connect();

        if (socket == null) {

            return null;
        }

        byte[] response = null;

        try {

            Transfer.writeUnsafe(socket, request);

            response = Transfer.readUnsafe(socket);

            socketPool.close(socket);

        } catch (Exception e) {

            System.out.println(e.getMessage());

            socketPool.remove(socket);
        }

        return response;
    }

    public static byte[] transfer(String host, int port, byte[] request) {

        byte[] receivedMessage = null;

        //Socket socket = new Socket();

        try (Socket socket = new Socket()) {

            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setSoTimeout(10000);

            Transfer.writeUnsafe(socket, request);

            receivedMessage = Transfer.readUnsafe(socket);

        } catch (Exception e) {

            //e.printStackTrace();

            //System.out.println(e.getMessage());
        }

        return receivedMessage;
    }

    //----------------------------------------------------------------------------

    public static final class Pool {

        private static final int EMPTY_SLOT = 0;
        private static final int READY_SLOT = 1;
        private static final int BUSY_SLOT = 2;

        private static final int BUSY_TIMEOUT = 60_000;
        private static final int PING_INTERVAL = 10_000;

        private final String address;
        private final int port;
        private final int capacity;
        private final int[] flags;
        private final long[] lastPull;
        private final Connection[] connections;
        private Checker checker;

        public Pool(String address, int port, int capacity) {

            this.address = address;
            this.port = port;
            this.capacity = capacity;
            this.flags = new int[capacity];
            this.lastPull = new long[capacity];
            this.connections = new Connection[capacity];
        }

        public Socket get() {

            Connection connection = reserve();

            if (connection == null) {

                connection = Connection.connect(address, port, BUSY_TIMEOUT);

                if (connection != null) {

                    add(connection);
                }
            }

            return connection;
        }

        public void pingAll() {

            long currentTime = System.currentTimeMillis();

            synchronized (flags) {

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] == BUSY_SLOT && currentTime - lastPull[index] > BUSY_TIMEOUT) {

                        flags[index] = EMPTY_SLOT;
                        connections[index].setPosition(-1);
                    }
                }
            }

            long time = System.currentTimeMillis() - PING_INTERVAL;

            Map<Integer, Connection> reservations = getAllPulledBefore(time);

            try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

                reservations.forEach((position, connection) -> executor.execute(() -> {

                    boolean wasSuccessful = false;

                    if (connection != null) {

                        wasSuccessful = checker.check(connection);
                    }

                    if (wasSuccessful) {

                        free(position, connection);

                    } else {

                        remove(position, connection);
                    }
                }));
            }
        }

        public Connection reserve() {

            Connection connection = null;

            synchronized (flags) {

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] == READY_SLOT) {

                        flags[index] = BUSY_SLOT;
                        lastPull[index] = System.currentTimeMillis();

                        connection = connections[index];
                        break;
                    }
                }
            }

            return connection;
        }

        public Map<Integer, Connection> getAllPulledBefore(long time) {

            Map<Integer, Connection> reservations = new LinkedHashMap<>();

            synchronized (flags) {

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] == READY_SLOT && lastPull[index] < time) {

                        flags[index] = BUSY_SLOT;
                        lastPull[index] = System.currentTimeMillis();

                        reservations.put(index, connections[index]);
                    }
                }
            }

            return reservations;
        }

        public void add(Connection connection) {

            synchronized (flags) {

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] == EMPTY_SLOT) {

                        flags[index] = BUSY_SLOT;
                        lastPull[index] = System.currentTimeMillis();
                        connections[index] = connection;

                        connection.setPosition(index);

                        System.out.println("SocketPool " + address + " [" + index + "] -> connected");

                        break;
                    }
                }
            }
        }

        public void free(Connection connection) {

            int position = connection.getPosition();

            free(position, connection);
        }

        public void free(int position, Connection connection) {

            if (position != -1) {

                synchronized (flags) {

                    flags[position] = READY_SLOT;
                }

            } else {

                Utilities.tryAndIgnore(connection::close);
            }
        }

        public void remove(int position, Connection connection) {

            if (position != -1) {

                synchronized (flags) {

                    flags[position] = EMPTY_SLOT;
                    connections[position] = null;
                }

                System.out.println("SocketPool " + address + " [" + position + "] -> removed");

            } else {

                Utilities.tryAndIgnore(connection::close);
            }
        }

        public void setChecker(Checker checker) {

            this.checker = checker;
        }

        public interface Checker {

            boolean check(Connection connection);
        }
    }

    public static class Connection extends Socket {

        private int position = -1;

        public static Connection connect(String address, int port, int timeout) {

            Connection socket = new Connection();

            try {

                socket.connect(new InetSocketAddress(address, port), 5000);
                socket.setSoTimeout(timeout);

                return socket;

            } catch (Exception e) {

                //e.printStackTrace();

                //System.out.println(e.getMessage());

                //BaseLogs.error(TAG, e);
            }

            return null;
        }

        public void setPosition(int position) {

            this.position = position;
        }

        public int getPosition() {

            return position;
        }
    }

    public static class Transfer {

        private static final int INSECURE_PREFIX_DATA_LENGTH = 10;

        public static void write(Socket socket, byte[] data) {

            try {

                writeUnsafe(socket, data);

            } catch (Exception ignore) {

            }
        }

        public static void writeUnsafe(Socket socket, byte[] data) throws IOException {

            OutputStream outputStream = socket.getOutputStream();

            byte[] buffer = new byte[INSECURE_PREFIX_DATA_LENGTH];

            int messageLength = data.length;

            for (int i = buffer.length - 1, j = 1; i >= 0; i--, j *= 10) {

                buffer[i] = (byte) ((messageLength / j) % 10);
            }

            outputStream.write(buffer);
            outputStream.write(data);
            outputStream.flush();
        }

        public static void write(Socket socket, InputStream inputStream, long messageLength) {

            write(socket, inputStream, messageLength, null);
        }

        public static void writeUnsafe(Socket socket, InputStream inputStream, long messageLength) throws IOException {

            writeUnsafe(socket, inputStream, messageLength, null);
        }

        public static void write(Socket socket, InputStream inputStream, long messageLength, ProgressListener progressListener) {

            try {

                writeUnsafe(socket, inputStream, messageLength, progressListener);

            } catch (Exception ignore) {

            }
        }

        public static void writeUnsafe(Socket socket, InputStream inputStream, long messageLength, ProgressListener progressListener) throws IOException {

            OutputStream outputStream = socket.getOutputStream();

            byte[] sizeBuffer = new byte[INSECURE_PREFIX_DATA_LENGTH];

            for (int i = sizeBuffer.length - 1, j = 1; i >= 0; i--, j *= 10) {

                sizeBuffer[i] = (byte) ((messageLength / j) % 10);
            }

            outputStream.write(sizeBuffer);

            int count;
            int totalSent = 0;

            byte[] streamBuffer = new byte[100 * 1024];

            while ((count = inputStream.read(streamBuffer)) > 0) {

                outputStream.write(streamBuffer, 0, count);
                totalSent += count;

                if (progressListener != null) {

                    progressListener.onProgress(totalSent, messageLength);
                }
            }

            outputStream.flush();
        }

        public static byte[] read(Socket socket) {

            return read(socket, null);
        }

        public static byte[] readUnsafe(Socket socket) throws IOException {

            return readUnsafe(socket, null);
        }

        public static byte[] read(Socket socket, ProgressListener progressListener) {

            try {

                return readUnsafe(socket, progressListener);

            } catch (IOException ignore) {

                return null;
            }
        }

        public static byte[] readUnsafe(Socket socket, ProgressListener progressListener) throws IOException {

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

                if (buffer[i] < 0 || buffer[i] >= 10) {

                    throw new IOException("DamagedData");
                }

                availableBytes += buffer[i] * j;
            }

            if (availableBytes < 0) {

                throw new IOException("DamagedData");
            }

            buffer = new byte[availableBytes];
            totalBytesRead = 0;
            bytesRead = 0;

            while (bytesRead != -1 && totalBytesRead < availableBytes) {

                bytesRead = inputStream.read(buffer, totalBytesRead, availableBytes - totalBytesRead);

                totalBytesRead += bytesRead;

                if (progressListener != null) {

                    progressListener.onProgress(totalBytesRead, availableBytes);
                }
            }

            return buffer;
        }

        public interface ProgressListener {

            void onProgress(long bytesWritten, long contentLength);
        }
    }
}