package com.chainedminds;

public class _Codes {

    public static final int NOT_FOUND = -1;
    public static final int UNDEFINED = NOT_FOUND;
    public static final int NOT_DEFINED = NOT_FOUND;

    public static final int RESPONSE_OK_CHANGE_UUID = 201;
    public static final int RESPONSE_OK_SYNC_INFO = 200;
    public static final int RESPONSE_OK = 100;
    public static final int RESPONSE_NOK = -100;

    public static final int RESPONSE_IS_NOT_REGISTERED = -100001;
    public static final int RESPONSE_CREDENTIAL_EXPIRED = -100002;
    public static final int RESPONSE_IS_REGISTERED_BEFORE = -100003;
    public static final int RESPONSE_INVALID_USERNAME_OR_PASSWORD = -100004;

    public static final int REQUEST_BASIC_DATA = 1;
    public static final int REQUEST_FILE_INFO = 2;
    public static final int REQUEST_FILE_BYTES = 3;
    public static final int REQUEST_FILE_HASH = 4;

    public static final int REQUEST_OPEN_PAGE = 1001;
    @Deprecated
    public static final int REQUEST_SET_NAME_DEPRECATED = 1023;
    public static final int REQUEST_BROADCAST_MESSAGE = 1047;
    public static final int REQUEST_OPEN_LOTTERY = 20007;
}
