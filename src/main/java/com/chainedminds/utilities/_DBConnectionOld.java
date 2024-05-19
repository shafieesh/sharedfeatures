package com.chainedminds.utilities;

import com.chainedminds._Config;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

@Deprecated
public class _DBConnectionOld {

    private static final String TAG = _DBConnectionOld.class.getSimpleName();

    public static final int DEFAULT_OPTIONS = 0;
    public static final int MANUAL_COMMIT = 1;

    private static ConnectionPool automaticConnections = null;
    private static ConnectionPool manualConnections = null;

    public static void config() {

        automaticConnections = new ConnectionPool("auto", 10);
        manualConnections = new ConnectionPool("manual", 10);

        ConnectionPool.ConnectionChecker connectionChecker = connection -> {

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {

                return resultSet.next();

            } catch (Exception e) {

                return false;
            }
        };

        automaticConnections.setConnectionChecker(connectionChecker);
        manualConnections.setConnectionChecker(connectionChecker);

        Task.add(Task.Data.build()
                .setName("Ping Connections")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 0, 10)
                .setTimingListener(task -> {

                    automaticConnections.pingConnections();
                    manualConnections.pingConnections();
                })
                .schedule());
    }

    public static Connection connect() {

        return connect(DEFAULT_OPTIONS);
    }

    public static Connection connect(int options) {

        Connection connection = null;

        try {

            if (options == DEFAULT_OPTIONS) {

                connection = automaticConnections.getConnection();
            }

            if (options == MANUAL_COMMIT) {

                connection = manualConnections.getConnection();

                if (connection != null) {

                    connection.setAutoCommit(false);
                }
            }

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return connection;
    }

    public static boolean close(Connection connection) {

        boolean wasSuccessful = false;

        if (connection != null) {

            CustomConnection customConnection = (CustomConnection) connection;

            String poolName = customConnection.getName();

            if ("auto".equals(poolName)) {

                automaticConnections.freeConnection(customConnection);
            }

            if ("manual".equals(poolName)) {

                manualConnections.freeConnection(customConnection);
            }

            wasSuccessful = true;
        }

        return wasSuccessful;
    }

    public static boolean commit(Connection connection) {

        boolean wasSuccessful = false;

        if (connection != null) {

            try {

                connection.commit();

                wasSuccessful = true;

            } catch (Exception e) {

                _Log.error(TAG, e);
            }
        }

        return wasSuccessful;
    }

    public static boolean rollback(Connection connection) {

        boolean wasSuccessful = false;

        if (connection != null) {

            try {

                connection.rollback();

                wasSuccessful = true;

            } catch (Exception e) {

                _Log.error(TAG, e);
            }
        }

        return wasSuccessful;
    }

    public static boolean commitOrRollback(Connection connection) {

        boolean wasSuccessful = commit(connection);

        if (!wasSuccessful) {

            rollback(connection);
        }

        return wasSuccessful;
    }

    public static class ConnectionPool {

        private static final int EMPTY_SLOT = 0;
        private static final int READY_SLOT = 1;
        private static final int BUSY_SLOT = 2;

        private static final int SLOW_QUERY_TIME = 30_000;

        private final String name;
        private final int capacity;
        private final int[] flags;
        private final long[] lastPull;
        private final CustomConnection[] connections;
        private ConnectionChecker connectionChecker;

        public ConnectionPool(String name, int capacity) {

            this.name = name;
            this.capacity = capacity;
            this.flags = new int[capacity];
            this.lastPull = new long[capacity];
            this.connections = new CustomConnection[capacity];
        }

        public void pingConnections() {

            long currentTime = System.currentTimeMillis();

            for (int index = 0; index < capacity; index++) {

                if (flags[index] == BUSY_SLOT && currentTime - lastPull[index] >= SLOW_QUERY_TIME) {

                    flags[index] = EMPTY_SLOT;
                    connections[index].setPosition(-1);
                }
            }

            for (int index = 0; index < capacity; index++) {

                if (flags[index] == READY_SLOT) {

                    flags[index] = BUSY_SLOT;
                    Connection connection = connections[index];

                    boolean wasSuccessful = false;

                    if (connection != null) {

                        connections[index].setLastCheckTime();
                        wasSuccessful = connectionChecker.check(connections[index]);
                    }

                    if (!wasSuccessful) {

                        flags[index] = EMPTY_SLOT;
                        connections[index] = null;

                        if (connection != null) {

                            Utilities.tryAndIgnore(connection::close);
                        }

                    } else {

                        flags[index] = READY_SLOT;
                    }
                }
            }
        }

        public Connection getConnection() {

            CustomConnection connection = null;

            for (int index = 0; index < capacity; index++) {

                if (flags[index] == READY_SLOT) {

                    //System.out.println(index + " is reused from pool");

                    flags[index] = BUSY_SLOT;
                    lastPull[index] = System.currentTimeMillis();

                    connection = connections[index];
                    break;
                }
            }

            if (connection == null) {

                connection = CustomConnection.create(name);
            }

            if (connection != null && connection.getPosition() == -1) {

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] == EMPTY_SLOT) {

                        flags[index] = BUSY_SLOT;
                        lastPull[index] = System.currentTimeMillis();
                        connections[index] = connection;

                        connection.setPosition(index);
                        break;
                    }
                }
            }

            return connection;
        }

        public void freeConnection(CustomConnection connection) {

            int position = connection.getPosition();

            if (position != -1) {

                flags[position] = READY_SLOT;

            } else {

                Utilities.tryAndIgnore(connection::close);
            }
        }

        public void setConnectionChecker(ConnectionChecker connectionChecker) {

            this.connectionChecker = connectionChecker;
        }

        public interface ConnectionChecker {

            boolean check(Connection connection);
        }
    }

    public static class CustomConnection implements Connection {

        private String name;
        private int position = -1;
        private final Connection connection;
        private long lastCheckTime = System.currentTimeMillis();

        public static CustomConnection create(String name) {

            //System.out.println("NEW CONNECTION");

            try {

                Connection connection = DriverManager.getConnection(
                        _Config.DATABASE_URL,
                        _Config.DATABASE_USERNAME,
                        _Config.DATABASE_PASSWORD);

                return new CustomConnection(name, connection);

            } catch (SQLException e) {

                e.printStackTrace();

                _Log.error(TAG, e);
            }

            return null;
        }

        public CustomConnection(String name, Connection connection) {

            this.connection = connection;
            this.name = name;
        }

        public CustomConnection(Connection connection) {

            this.connection = connection;
        }

        public void setPosition(int position) {

            this.position = position;
        }

        public void setLastCheckTime() {

            lastCheckTime = System.currentTimeMillis();
        }

        public long getLastCheckTime() {

            return lastCheckTime;
        }

        public int getPosition() {

            return position;
        }

        public String getName() {

            return name;
        }

        @Override
        public Statement createStatement() throws SQLException {

            return connection.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {

            return connection.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {

            return connection.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {

            return connection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {

            connection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {

            return connection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {

            connection.commit();
        }

        @Override
        public void rollback() throws SQLException {

            connection.rollback();
        }

        @Override
        public void close() throws SQLException {

            connection.close();
        }

        @Override
        public boolean isClosed() throws SQLException {

            return connection.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {

            return connection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {

            connection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {

            return connection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {

            connection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {

            return connection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {

            connection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {

            return connection.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {

            return connection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {

            connection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {

            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {

            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {

            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {

            return connection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

            connection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {

            connection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {

            return connection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {

            return connection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {

            return connection.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {

            connection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {

            connection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {

            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {

            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {

            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {

            return connection.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {

            return connection.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {

            return connection.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {

            return connection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {

            return connection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {

            return connection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {

            return connection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {

            return connection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {

            connection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {

            connection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {

            return connection.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {

            return connection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {

            return connection.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {

            return connection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {

            connection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {

            return connection.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {

            connection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

            connection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {

            return connection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {

            return connection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {

            return connection.isWrapperFor(iface);
        }
    }
}