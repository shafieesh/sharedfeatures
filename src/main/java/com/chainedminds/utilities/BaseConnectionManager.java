package com.chainedminds.utilities;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseConnectionManager {

    private static final String TAG = BaseConnectionManager.class.getSimpleName();

    public static final int DEFAULT_OPTIONS = 0;
    public static final int MANUAL_COMMIT = 1;

    private ConnectionPool automaticConnections = null;
    private ConnectionPool manualConnections = null;

    protected String getAddress() {

        return null;
    }

    protected String getUsername() {

        return null;
    }

    protected String getPassword() {

        return null;
    }

    public final void config(int autoCommitConnectionsPoolSize, int manualCommitConnectionsPoolSize) {

        automaticConnections = new ConnectionPool("auto", autoCommitConnectionsPoolSize);
        manualConnections = new ConnectionPool("manual", manualCommitConnectionsPoolSize);

        ConnectionPool.ConnectionChecker connectionChecker = connection -> {

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {

                return resultSet.next();
            }
        };

        automaticConnections.setConnectionChecker(connectionChecker);
        manualConnections.setConnectionChecker(connectionChecker);
    }

    public final Connection getConnection() {

        return getConnection(DEFAULT_OPTIONS);
    }

    public final Connection getConnection(int options) {

        Connection connection = null;

        for (int count = 0; count < 3; count++) {

            try {

                if (options == DEFAULT_OPTIONS) {

                    String address = getAddress();
                    String username = getUsername();
                    String password = getPassword();

                    connection = automaticConnections.getConnection(address, username, password);
                }

                if (options == MANUAL_COMMIT) {

                    String address = getAddress();
                    String username = getUsername();
                    String password = getPassword();

                    connection = manualConnections.getConnection(address, username, password);

                    if (connection != null) {

                        connection.setAutoCommit(false);
                    }
                }

                if (connection != null) {

                    break;
                }

            } catch (Exception e) {

                Log.error(TAG, e);
            }
        }

        return connection;
    }

    public final boolean close(Connection connection) {

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

    public final boolean commit(Connection connection) {

        boolean wasSuccessful = false;

        if (connection != null) {

            try {

                connection.commit();

                wasSuccessful = true;

            } catch (Exception e) {

                Log.error(TAG, e);
            }
        }

        return wasSuccessful;
    }

    public final boolean rollback(Connection connection) {

        boolean wasSuccessful = false;

        if (connection != null) {

            try {

                connection.rollback();

                wasSuccessful = true;

            } catch (Exception e) {

                Log.error(TAG, e);
            }
        }

        return wasSuccessful;
    }

    public final boolean commitOrRollback(Connection connection) {

        boolean wasSuccessful = commit(connection);

        if (!wasSuccessful) {

            rollback(connection);
        }

        return wasSuccessful;
    }

    private static final class ConnectionPool {

        private static final String TAG = ConnectionPool.class.getSimpleName();

        private static final int NOT_AVAILABLE = 0;
        private static final int AVAILABLE = 1;
        private static final int IN_USE = 2;

        private static final int BUSY_ITEM_TIMEOUT = 20_000;

        private final String name;
        private final int capacity;
        private final int[] flags;
        private final long[] lastUses;
        private final CustomConnection[] connections;
        private ConnectionChecker connectionChecker;
        private final ReentrantLock LOCK = new ReentrantLock();

        public ConnectionPool(String name, int capacity) {

            this.name = name;
            this.capacity = capacity;
            this.flags = new int[capacity];
            this.lastUses = new long[capacity];
            this.connections = new CustomConnection[capacity];
        }

        public Connection getConnection(String address, String username, String password) {

            long currentTime = System.currentTimeMillis();

            AtomicReference<CustomConnection> connection = new AtomicReference<>();

            Utilities.lock(TAG, LOCK, () -> {

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] != NOT_AVAILABLE && currentTime - lastUses[index] >= BUSY_ITEM_TIMEOUT) {

                        //System.out.println(index + " is removing from pool");

                        flags[index] = NOT_AVAILABLE;

                        connections[index].setPosition(-1);
                    }
                }

                for (int index = 0; index < capacity; index++) {

                    if (flags[index] == AVAILABLE) {

                        //System.out.println(index + " is reused from pool");

                        flags[index] = IN_USE;
                        lastUses[index] = currentTime;

                        connection.set(connections[index]);

                        break;
                    }
                }
            });

            if (connection.get() == null) {

                connection.set(CustomConnection.create(name, address, username, password));
            }

            if (connection.get() != null) {

                if (connection.get().getPosition() == -1) {

                    Utilities.lock(TAG, LOCK, () -> {

                        for (int index = 0; index < capacity; index++) {

                            if (flags[index] == NOT_AVAILABLE) {

                                flags[index] = IN_USE;
                                lastUses[index] = currentTime;
                                connections[index] = connection.get();

                                connection.get().setPosition(index);

                                break;
                            }
                        }
                    });
                }

                boolean wasSuccessful = false;

                if (System.currentTimeMillis() - connection.get().getLastCheckTime() > 15_000) {

                    try {

                        wasSuccessful = connectionChecker == null || connectionChecker.check(connection.get());

                        connection.get().setLastCheckTime();

                    } catch (SQLException e) {

                        Log.error(TAG, e);
                    }

                } else {

                    wasSuccessful = true;
                }

                if (!wasSuccessful) {

                    Utilities.tryAndIgnore(() -> connection.get().close());

                    int position = connection.get().getPosition();

                    if (position != -1) {

                        Utilities.lock(TAG, LOCK, () -> {

                            flags[position] = NOT_AVAILABLE;

                            connections[position] = null;
                        });
                    }

                    return null;
                }
            }

            return connection.get();
        }

        public void freeConnection(CustomConnection connection) {

            int position = connection.getPosition();

            if (position != -1) {

                Utilities.lock(TAG, LOCK, () -> {

                    flags[position] = AVAILABLE;
                });

            } else {

                Utilities.tryAndIgnore(connection::close);
            }
        }

        public void setConnectionChecker(ConnectionChecker connectionChecker) {

            this.connectionChecker = connectionChecker;
        }

        public interface ConnectionChecker {

            boolean check(Connection connection) throws SQLException;
        }
    }

    public static class CustomConnection implements Connection {

        private String name;
        private int position = -1;
        private final Connection connection;
        private long lastCheckTime;

        public static CustomConnection create(String name, String address, String username, String password) {

            //System.out.println("NEW CONNECTION");

            try {

                Connection connection = DriverManager.getConnection(address, username, password);

                return new CustomConnection(name, connection);

            } catch (SQLException e) {

                e.printStackTrace();

                Log.error(TAG, e);
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