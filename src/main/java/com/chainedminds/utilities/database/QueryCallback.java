package com.chainedminds.utilities.database;

import java.sql.ResultSet;

public abstract class QueryCallback {

    public void fetch(ResultSet resultSet) throws Exception {}

    public void finalize(boolean wasSuccessful, Exception error) {}
}

