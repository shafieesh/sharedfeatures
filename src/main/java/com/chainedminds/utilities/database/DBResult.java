package com.chainedminds.utilities.database;

public class DBResult<T> {

    public T value;
    public Exception error;

    public boolean isSuccessful() {

        return error == null;
    }
}