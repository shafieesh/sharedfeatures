package com.chainedminds.utilities;

import com.chainedminds.models.BaseData;

public class BackendManager {

    private static final String TAG = BackendManager.class.getSimpleName();

    private static final String BACKEND_ACCOUNT_MANAGER = "AccountManager";
    private static final String BACKEND_LEADERBOARD_MANAGER = "LeaderboardManager";

    private static final String[] AVAILABLE_BACKEND = {
            BACKEND_ACCOUNT_MANAGER,
            BACKEND_LEADERBOARD_MANAGER
    };

    public static BaseData register(BaseData data) {

        //String channelID = data.client.channelID;


        return null;
    }
}
