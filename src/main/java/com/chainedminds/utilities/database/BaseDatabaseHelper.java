package com.chainedminds.utilities.database;

import com.chainedminds.utilities.BaseLogs;
import com.chainedminds.utilities.Utilities;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseDatabaseHelper {

    private int queriesCount = 0;
    private int updatesCount = 0;
    private int insertsCount = 0;

    protected Connection getConnection() {

        return null;
    }

    protected void closeConnection(Connection connection) {

    }

    public final Map<String, Integer> getConnectionsCount() {

        int copiedQueriesCount = queriesCount;
        int copiedUpdatesCount = updatesCount;
        int copiedInsertsCount = insertsCount;
        int totalCount = copiedQueriesCount + copiedUpdatesCount + copiedInsertsCount;

        queriesCount = 0;
        updatesCount = 0;
        insertsCount = 0;

        Map<String, Integer> connectionsCount = new HashMap<>();
        connectionsCount.put("queries", copiedQueriesCount);
        connectionsCount.put("updates", copiedUpdatesCount);
        connectionsCount.put("inserts", copiedInsertsCount);
        connectionsCount.put("total", totalCount);

        return connectionsCount;
    }

    public final boolean query(String tag, String statement) {

        return query(tag, statement, null, (QueryCallback) null);
    }

    public final boolean query(Connection connection, String tag, String statement) {

        return query(connection, tag, statement, null, (QueryCallback) null);
    }

    public final boolean query(String tag, String statement, Map<Integer, Object> parameters) {

        Connection connection = getConnection();

        boolean wasSuccessful = query(connection, tag, statement, parameters);

        closeConnection(connection);

        return wasSuccessful;
    }

    //----------

    public final boolean query(String tag, String statement, QueryCallback queryCallback) {

        return query(tag, statement, null, queryCallback);
    }

    public final boolean query(String tag, String statement, TwoStepQueryCallback queryCallback) {

        return query(tag, statement, null, queryCallback);
    }

    //----------

    public final boolean query(String tag, String statement, Map<Integer, Object> parameters, QueryCallback queryCallback) {

        Connection connection = getConnection();

        boolean wasSuccessful = query(connection, tag, statement, parameters, queryCallback);

        closeConnection(connection);

        return wasSuccessful;
    }

    public final boolean query(String tag, String statement, Map<Integer, Object> parameters, TwoStepQueryCallback queryCallback) {

        Connection connection = getConnection();

        boolean wasSuccessful = query(connection, tag, statement, parameters, queryCallback);

        closeConnection(connection);

        return wasSuccessful;
    }

    //----------

    public final boolean query(Connection connection, String tag, String statement, QueryCallback queryCallback) {

        return query(connection, tag, statement, null, queryCallback);
    }

    public final boolean query(Connection connection, String tag, String statement, TwoStepQueryCallback queryCallback) {

        return query(connection, tag, statement, null, queryCallback);
    }

    //----------

    public final boolean query(Connection connection, String tag, String statement, Map<Integer, Object> parameters) {

        return query(connection, tag, statement, parameters, (QueryCallback) null);
    }

    public final boolean query(Connection connection,
                               String tag, String statement,
                               Map<Integer, Object> parameters,
                               TwoStepQueryCallback queryCallback) {

        queriesCount++;

        AtomicReference<Exception> error = new AtomicReference<>();

        boolean wasSuccessful = false;
        PreparedStatement preparedStatement = null;
        ResultSet results = null;

        try {

            if (connection == null) {

                throw new Exception("Cannot establish a connection to database.");
            }

            preparedStatement = connection.prepareStatement(statement);

            if (parameters != null) {

                for (int key : parameters.keySet()) {

                    Object value = parameters.get(key);

                    preparedStatement.setObject(key, value);
                }
            }

            results = preparedStatement.executeQuery();

            if (queryCallback != null) {

                queryCallback.onFetchingData(results);
            }

            wasSuccessful = true;

        } catch (Exception exception) {

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            BaseLogs.error(tag, exception, payload);

            error.set(exception);

        } finally {

            if (parameters != null) {

                parameters.clear();
            }

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (results != null) {

                Utilities.tryAndIgnore(results::close);
            }

            if (queryCallback != null) {

                if (wasSuccessful) {

                    Utilities.tryAndCatch(tag, () -> queryCallback.onFinishedTask(true, null));

                } else {

                    Utilities.tryAndCatch(tag, () -> queryCallback.onFinishedTask(false, error.get()));
                }
            }
        }

        return wasSuccessful;
    }

    public final boolean query(Connection connection,
                               String tag, String statement,
                               Map<Integer, Object> parameters,
                               QueryCallback queryCallback) {

        queriesCount++;

        boolean wasSuccessful = false;
        PreparedStatement preparedStatement = null;
        ResultSet results = null;

        try {

            if (connection == null) {

                throw new Exception("Cannot establish a connection to database.");
            }

            preparedStatement = connection.prepareStatement(statement);

            if (parameters != null) {

                for (int key : parameters.keySet()) {

                    Object value = parameters.get(key);

                    preparedStatement.setObject(key, value);
                }
            }

            results = preparedStatement.executeQuery();

            if (queryCallback != null) {

                queryCallback.onFetchingData(results);
            }

            wasSuccessful = true;

        /*} catch (SQLException error) {

            error.getErrorCode();
            error.getSQLState()
            error.getMessage()

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            BaseLogs.error(tag, error, payload);
        }*/
        } catch (Exception error) {

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            BaseLogs.error(tag, error, payload);

        } finally {

            if (parameters != null) {

                parameters.clear();
            }

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (results != null) {

                Utilities.tryAndIgnore(results::close);
            }
        }

        return wasSuccessful;
    }

    //---------------------------------------------------------------------------------

    public final boolean update(String tag, String statement) {

        return update(tag, statement, null, null);
    }

    public final boolean update(Connection connection, String tag, String statement) {

        return update(connection, tag, statement, null, null);
    }

    public final boolean update(String tag, String statement, UpdateCallback updateCallback) {

        return update(tag, statement, null, updateCallback);
    }

    public final boolean update(Connection connection, String tag, String statement, UpdateCallback updateCallback) {

        return update(connection, tag, statement, null, updateCallback);
    }

    public final boolean update(String tag, String statement, Map<Integer, Object> parameters) {

        return update(tag, statement, parameters, null);
    }

    public final boolean update(Connection connection, String tag, String statement, Map<Integer, Object> parameters) {

        return update(connection, tag, statement, parameters, null);
    }

    public final boolean update(String tag, String statement, Map<Integer, Object> parameters, UpdateCallback updateCallback) {

        Connection connection = getConnection();

        boolean wasSuccessful = update(connection, tag, statement, parameters, updateCallback);

        closeConnection(connection);

        return wasSuccessful;
    }

    public final boolean update(Connection connection, String tag, String statement,
                                Map<Integer, Object> parameters,
                                UpdateCallback updateCallback) {

        updatesCount++;

        AtomicReference<Exception> error = new AtomicReference<>();

        boolean wasSuccessful = false;
        PreparedStatement preparedStatement = null;

        try {

            if (connection == null) {

                throw new Exception("Cannot establish a connection to database.");
            }

            preparedStatement = connection.prepareStatement(statement);

            if (parameters != null) {

                for (int key : parameters.keySet()) {

                    preparedStatement.setObject(key, parameters.get(key));
                }
            }

            preparedStatement.execute();

            wasSuccessful = true;

        } catch (Exception exception) {

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            BaseLogs.error(tag, exception, payload);

            error.set(exception);

        } finally {

            if (parameters != null) {

                parameters.clear();
            }

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (updateCallback != null) {

                if (wasSuccessful) {

                    Utilities.tryAndCatch(tag, () -> updateCallback.run(true, null));

                } else {

                    Utilities.tryAndCatch(tag, () -> updateCallback.run(false, error.get()));
                }
            }
        }

        return wasSuccessful;
    }

    public final boolean insert(String tag, String statement) {

        return insert(tag, statement, null, null);
    }

    public final boolean insert(Connection connection, String tag, String statement) {

        return insert(connection, tag, statement, null, null);
    }

    public final boolean insert(String tag, String statement, InsertCallback insertCallback) {

        return insert(tag, statement, null, insertCallback);
    }

    public final boolean insert(Connection connection, String tag, String statement, InsertCallback insertCallback) {

        return insert(connection, tag, statement, null, insertCallback);
    }

    public final boolean insert(String tag, String statement, Map<Integer, Object> parameters) {

        return insert(tag, statement, parameters, null);
    }

    public final boolean insert(Connection connection, String tag, String statement, Map<Integer, Object> parameters) {

        return insert(connection, tag, statement, parameters, null);
    }

    public final boolean insert(String tag, String statement, Map<Integer, Object> parameters, InsertCallback insertCallback) {

        Connection connection = getConnection();

        boolean wasSuccessful = insert(connection, tag, statement, parameters, insertCallback);

        closeConnection(connection);

        return wasSuccessful;
    }

    public final boolean insert(Connection connection, String tag, String statement,
                                Map<Integer, Object> parameters,
                                InsertCallback insertCallback) {

        insertsCount++;

        AtomicInteger generatedID = new AtomicInteger();
        AtomicReference<Exception> error = new AtomicReference<>();

        boolean wasSuccessful = false;
        PreparedStatement preparedStatement = null;
        ResultSet results = null;

        try {

            if (connection == null) {

                throw new Exception("Cannot establish a connection to database.");
            }

            preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);

            if (parameters != null) {

                for (int key : parameters.keySet()) {

                    Object value = parameters.get(key);

                    preparedStatement.setObject(key, value);
                }
            }

            preparedStatement.execute();

            if (insertCallback != null) {

                results = preparedStatement.getGeneratedKeys();

                generatedID.set(results.next() ? results.getInt(1) : 0);
            }

            wasSuccessful = true;

        } catch (Exception exception) {

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            BaseLogs.error(tag, exception, payload);

            error.set(exception);

        } finally {

            if (parameters != null) {

                parameters.clear();
            }

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (results != null) {

                Utilities.tryAndIgnore(results::close);
            }

            if (insertCallback != null) {

                if (wasSuccessful) {

                    Utilities.tryAndCatch(tag, () -> insertCallback.onFinishedTask(true, generatedID.get(), null));

                } else {

                    Utilities.tryAndCatch(tag, () -> insertCallback.onFinishedTask(false, 0, error.get()));
                }
            }
        }

        return wasSuccessful;
    }
}