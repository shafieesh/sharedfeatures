package com.chainedminds.utilities.database;

public interface UpdateCallback {

    void finalize(boolean wasSuccessful, Exception error);
}

