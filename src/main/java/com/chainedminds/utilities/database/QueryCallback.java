package com.chainedminds.utilities.database;

import java.sql.ResultSet;

public interface QueryCallback {

    void onFetchingData(ResultSet resultSet) throws Exception;
}

