package com.chainedminds.utilities.database;

public interface InsertCallback {

    void onFinishedTask(boolean wasSuccessful, int generatedID, Exception error);
}

