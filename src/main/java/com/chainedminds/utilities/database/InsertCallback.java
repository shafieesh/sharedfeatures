package com.chainedminds.utilities.database;

public interface InsertCallback {

    void finalize(boolean wasSuccessful, int generatedID, Exception error);
}

