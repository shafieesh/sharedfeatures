package com.chainedminds.utilities;

import com.chainedminds.BaseCodes;
import com.chainedminds.dataClasses.BaseData;

public class BaseManager<Data extends BaseData> {

    private boolean initialization = false;

    protected void setInitialization(boolean done) {

        initialization = done;
    }

    protected boolean checkInitialization() {

        return !initialization;
    }

    protected Data handleNotInitialized(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        return data;
    }

    protected Data handleMissingData(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;
        data.message = Messages.get("SYSTEM_GENERAL", Messages.General.MISSING_DATA, data.client.language);

        return data;
    }
}