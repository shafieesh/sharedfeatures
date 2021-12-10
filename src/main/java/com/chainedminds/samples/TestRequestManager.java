package com.chainedminds.samples;

import com.chainedminds.BaseCodes;
import com.chainedminds.api.BaseRequestsManager;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.BaseFileData;
import com.chainedminds.utilities.json.JsonHelper;

import java.net.Socket;

public class TestRequestManager extends BaseRequestsManager<BaseData> {

    private static final String TAG = TestRequestManager.class.getSimpleName();

    public TestRequestManager(Class<BaseData> mappedClass) {

        super(mappedClass);
    }

    @Override
    public Object handleRequest(BaseData data, Socket socket) {

        System.out.println(JsonHelper.getString(data));

        BaseData responseData = new BaseData();
        responseData.response = BaseCodes.RESPONSE_NOK;

        return responseData;
    }
}