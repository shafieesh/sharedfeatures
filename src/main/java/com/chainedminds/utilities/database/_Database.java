package com.chainedminds.utilities.database;

import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities._Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class _Database {

    private int queries = 0;
    private int updates = 0;
    private int inserts = 0;

    public Connection connect() {

        return null;
    }

    public void close(Connection connection) {

    }

    public final Map<String, Integer> getConnectionsCount() {

        int copiedQueriesCount = queries;
        int copiedUpdatesCount = updates;
        int copiedInsertsCount = inserts;
        int totalCount = copiedQueriesCount + copiedUpdatesCount + copiedInsertsCount;

        queries = 0;
        updates = 0;
        inserts = 0;

        Map<String, Integer> connectionsCount = new HashMap<>();
        connectionsCount.put("queries", copiedQueriesCount);
        connectionsCount.put("updates", copiedUpdatesCount);
        connectionsCount.put("inserts", copiedInsertsCount);
        connectionsCount.put("total", totalCount);

        return connectionsCount;
    }

    //----------

    public final boolean query(String tag, String statement, QueryCallback callback) {

        return query(tag, statement, (Map<Integer, Object>) null, callback);
    }

    public final boolean query(String tag, String statement,
                               List<Object> parameters, QueryCallback callback) {

        Map<Integer, Object> indexedParameters = new LinkedHashMap<>();

        for (int index = 0; index < parameters.size(); index++) {

            indexedParameters.put(index + 1, parameters.get(index));
        }

        return query(tag, statement, indexedParameters, callback);
    }

    public final boolean query(Connection connection, String tag, String statement, QueryCallback callback) {

        return query(connection, tag, statement, (Map<Integer, Object>) null, callback);
    }

    public final boolean query(Connection connection, String tag, String statement,
                               List<Object> parameters, QueryCallback callback) {

        Map<Integer, Object> indexedParameters = new LinkedHashMap<>();

        for (int index = 0; index < parameters.size(); index++) {

            indexedParameters.put(index + 1, parameters.get(index));
        }

        return query(connection, tag, statement, indexedParameters, callback);
    }

    public final boolean query(String tag, String statement,
                               Map<Integer, Object> parameters, QueryCallback callback) {

        Connection connection = connect();

        boolean wasSuccessful = query(connection, tag, statement, parameters, callback);

        close(connection);

        return wasSuccessful;
    }

    public final boolean query(Connection connection, String tag, String statement,
                               Map<Integer, Object> parameters, QueryCallback callback) {

        queries++;

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

            if (callback != null) {

                callback.fetch(results);
            }

            wasSuccessful = true;

        } catch (Exception exception) {

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            _Log.error(tag, exception, payload);

            error.set(exception);

        } finally {

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (results != null) {

                Utilities.tryAndIgnore(results::close);
            }

            if (callback != null) {

                if (wasSuccessful) {

                    Utilities.tryAndLog(tag, () -> callback.finalize(true, null));

                } else {

                    Utilities.tryAndLog(tag, () -> callback.finalize(false, error.get()));
                }
            }
        }

        return wasSuccessful;
    }

    //---------------------------------------------------------------------------------

    public final boolean update(String tag, String statement, UpdateCallback callback) {

        return update(tag, statement, (Map<Integer, Object>) null, callback);
    }

    public final boolean update(String tag, String statement, List<Object> parameters, UpdateCallback callback) {

        Map<Integer, Object> indexedParameters = new LinkedHashMap<>();

        for (int index = 0; index < parameters.size(); index++) {

            indexedParameters.put(index + 1, parameters.get(index));
        }

        return update(tag, statement, indexedParameters, callback);
    }

    public final boolean update(Connection connection, String tag, String statement, UpdateCallback callback) {

        return update(connection, tag, statement, (Map<Integer, Object>) null, callback);
    }

    public final boolean update(Connection connection, String tag, String statement, List<Object> parameters, UpdateCallback callback) {

        Map<Integer, Object> indexedParameters = new LinkedHashMap<>();

        for (int index = 0; index < parameters.size(); index++) {

            indexedParameters.put(index + 1, parameters.get(index));
        }

        return update(connection, tag, statement, indexedParameters, callback);
    }

    public final boolean update(String tag, String statement, Map<Integer, Object> parameters, UpdateCallback callback) {

        Connection connection = connect();

        boolean wasSuccessful = update(connection, tag, statement, parameters, callback);

        close(connection);

        return wasSuccessful;
    }

    public final boolean update(Connection connection, String tag, String statement,
                                Map<Integer, Object> parameters, UpdateCallback callback) {

        updates++;

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

            _Log.error(tag, exception, payload);

            error.set(exception);

        } finally {

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (callback != null) {

                if (wasSuccessful) {

                    Utilities.tryAndLog(tag, () -> callback.finalize(true, null));

                } else {

                    Utilities.tryAndLog(tag, () -> callback.finalize(false, error.get()));
                }
            }
        }

        return wasSuccessful;
    }

    //---------------------------------------------------------------------------------

    public final boolean insert(String tag, String statement, InsertCallback callback) {

        return insert(tag, statement, (Map<Integer, Object>) null, callback);
    }

    public final boolean insert(String tag, String statement, List<Object> parameters, InsertCallback callback) {

        Map<Integer, Object> indexedParameters = new LinkedHashMap<>();

        for (int index = 0; index < parameters.size(); index++) {

            indexedParameters.put(index + 1, parameters.get(index));
        }

        return insert(tag, statement, indexedParameters, callback);
    }

    public final boolean insert(Connection connection, String tag, String statement, InsertCallback callback) {

        return insert(connection, tag, statement, (Map<Integer, Object>) null, callback);
    }

    public final boolean insert(Connection connection, String tag, String statement, List<Object> parameters, InsertCallback callback) {

        Map<Integer, Object> indexedParameters = new LinkedHashMap<>();

        for (int index = 0; index < parameters.size(); index++) {

            indexedParameters.put(index + 1, parameters.get(index));
        }

        return insert(connection, tag, statement, indexedParameters, callback);
    }

    public final boolean insert(String tag, String statement, Map<Integer, Object> parameters, InsertCallback callback) {

        Connection connection = connect();

        boolean wasSuccessful = insert(connection, tag, statement, parameters, callback);

        close(connection);

        return wasSuccessful;
    }

    public final boolean insert(Connection connection, String tag, String statement,
                                Map<Integer, Object> parameters, InsertCallback callback) {

        inserts++;

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

            if (callback != null) {

                results = preparedStatement.getGeneratedKeys();

                generatedID.set(results.next() ? results.getInt(1) : 0);
            }

            wasSuccessful = true;

        } catch (Exception exception) {

            String payload = null;

            if (preparedStatement != null) {

                payload = preparedStatement.toString();
            }

            _Log.error(tag, exception, payload);

            error.set(exception);

        } finally {

            if (preparedStatement != null) {

                Utilities.tryAndIgnore(preparedStatement::close);
            }

            if (results != null) {

                Utilities.tryAndIgnore(results::close);
            }

            if (callback != null) {

                if (wasSuccessful) {

                    Utilities.tryAndLog(tag, () -> callback.finalize(true, generatedID.get(), null));

                } else {

                    Utilities.tryAndLog(tag, () -> callback.finalize(false, 0, error.get()));
                }
            }
        }

        return wasSuccessful;
    }
}