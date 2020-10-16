package com.chainedminds.utilities.database;

import java.sql.ResultSet;

public interface TwoStepQueryCallback {

    void onFetchingData(ResultSet resultSet) throws Exception;

    void onFinishedTask(boolean wasSuccessful, Exception error);
}

