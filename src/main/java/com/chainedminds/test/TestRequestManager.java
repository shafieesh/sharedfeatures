package com.chainedminds.test;

import com.chainedminds.BaseCodes;
import com.chainedminds.api.BaseRequestsManager;
import com.chainedminds.models.BaseData;
import com.chainedminds.utilities.json.JsonHelper;

import java.net.Socket;

public class TestRequestManager extends BaseRequestsManager<TestData> {

    private static final String TAG = TestRequestManager.class.getSimpleName();

    public TestRequestManager(Class<TestData> mappedClass) {

        super(mappedClass);
    }

    @Override
    public Object handleRequest(TestData data, Socket socket) {

        System.out.println(JsonHelper.getString(data));

        BaseData<?> responseData = new BaseData<>();
        responseData.response = BaseCodes.RESPONSE_NOK;

        return responseData;
    }
}