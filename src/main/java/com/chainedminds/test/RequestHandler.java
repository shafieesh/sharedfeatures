package com.chainedminds.test;

import com.chainedminds._Codes;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.models._Data;
import com.chainedminds.utilities.json.Json;

import java.net.Socket;

public class RequestHandler extends _RequestHandler<Data> {

    private static final String TAG = RequestHandler.class.getSimpleName();

    public RequestHandler(Class<Data> mappedClass) {

        super(mappedClass);
    }

    @Override
    public Object handleRequest(Data data, Socket socket) {

        System.out.println(Json.getString(data));

        _Data<?> responseData = new _Data<>();
        responseData.response = _Codes.RESPONSE_NOK;

        return responseData;
    }
}