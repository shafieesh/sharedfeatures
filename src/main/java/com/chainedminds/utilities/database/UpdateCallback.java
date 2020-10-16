package com.chainedminds.utilities.database;

public interface UpdateCallback {

    void run(boolean wasSuccessful, Exception error);
}

