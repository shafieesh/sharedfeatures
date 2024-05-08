package com.chainedminds.utilities.database;

import java.sql.ResultSet;

public abstract class TwoStepQueryCallback {

    public void onFetchingData(ResultSet resultSet) throws Exception {}

    public void onFinishedTask(boolean wasSuccessful, Exception error) {}
}

