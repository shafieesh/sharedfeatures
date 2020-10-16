package com.chainedminds.utilities;

public interface HttpResponseCallback {

    void onHttpResponse(int responseCode, String receivedMessage);
}
