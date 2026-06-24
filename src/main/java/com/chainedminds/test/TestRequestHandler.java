package com.chainedminds.test;

import com.chainedminds._Codes;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.models._Data;
import com.chainedminds.utilities.json.Json;

import java.net.Socket;

public class TestRequestHandler extends _RequestHandler<TestData> {

    private static final String TAG = TestRequestHandler.class.getSimpleName();

    public TestRequestHandler(Class<TestData> mappedClass) {

        super(mappedClass);
    }

    @Override
    public Object handleRequest(TestData data, Socket socket) {

        System.out.println(Json.getString(data));

        _Data<?> responseData = new _Data<>();
        responseData.response = _Codes.RESPONSE_NOK;

        return responseData;
    }
}