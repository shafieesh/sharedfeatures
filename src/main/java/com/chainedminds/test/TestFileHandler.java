package com.chainedminds.test;

import com.chainedminds._Codes;
import com.chainedminds.api._FileHandler;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.models._Data;
import com.chainedminds.utilities.json.Json;

import java.net.Socket;
import java.util.Arrays;

public class TestFileHandler extends _FileHandler<TestData> {

    private static final String TAG = TestFileHandler.class.getSimpleName();

    public TestFileHandler(Class<TestData> mappedClass) {

        super(mappedClass);
    }

    @Override
    public Object handleRequest(TestData request, byte[] data) {

        System.out.println(Json.getString(request));
        System.out.println(Arrays.toString(data));

        _Data<?> responseData = new _Data<>();
        responseData.response = _Codes.RESPONSE_NOK;

        return responseData;
    }
}