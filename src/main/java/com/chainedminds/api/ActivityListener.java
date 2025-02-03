package com.chainedminds.api;

import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.utilities._NotificationManager;
import com.chainedminds.utilities.Task;
import com.chainedminds.utilities.database._DatabaseOld;

import java.util.HashMap;
import java.util.Map;

public class ActivityListener {

    private static final String TAG = ActivityListener.class.getSimpleName();

    private static final String FIELD_AGENT_ID = "AgentID";
    private static final String FIELD_TARGET_ID = "TargetID";

    private static final int BECAME_ONLINE = 1;
    private static final int ENTERED_CHAT_ROOM = 2;
    private static final int LEFT_CHAT_ROOM = 3;

    private static final Map<Integer, Integer> RELATIONS = new HashMap<>();
    private static final Map<Integer, Integer> LAST_TARGET_ACTIONS = new HashMap<>();
    private static final Map<Integer, String> TARGETS_NAMES = new HashMap<>();

    public static void start() {

        Task.add(Task.Data.build()
                .setName("RefreshActivityFinderList")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .setTimingListener(task -> fetch())
                .runAsyncNow()
                .schedule());
    }

    private static void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_ACTIVITY_FINDER;

        _DatabaseOld.query(TAG, selectStatement, resultSet -> {

            Map<Integer, Integer> relations = new HashMap<>();

            while (resultSet.next()) {

                relations.put(resultSet.getInt(FIELD_TARGET_ID), resultSet.getInt(FIELD_AGENT_ID));
            }

            Map<Integer, String> targetNames = new HashMap<>();

            for (int targetID : relations.keySet()) {

                targetNames.put(targetID, _Resources.get().account.getName(targetID));
            }

            for (int targetID : RELATIONS.keySet()) {

                if (!relations.containsKey(targetID)) {

                    LAST_TARGET_ACTIONS.remove(targetID);
                }
            }

            RELATIONS.clear();
            RELATIONS.putAll(relations);

            TARGETS_NAMES.clear();
            TARGETS_NAMES.putAll(targetNames);

            relations.clear();
            targetNames.clear();
        });
    }

    public static void setBecameOnline(int targetID) {

        if (RELATIONS.containsKey(targetID) && LAST_TARGET_ACTIONS.getOrDefault(targetID, 0) != BECAME_ONLINE) {

            LAST_TARGET_ACTIONS.put(targetID, BECAME_ONLINE);

            String targetName = TARGETS_NAMES.get(targetID);

            _NotificationManager.sendNotification(RELATIONS.get(targetID), _Config.APP_NAME_CAFE_GAME,
                    "Activity Reporter", targetName + " has became online.");
        }
    }

    public static void setEnteredChatRoom(int targetID) {

        if (RELATIONS.containsKey(targetID) && LAST_TARGET_ACTIONS.getOrDefault(targetID, 0) != ENTERED_CHAT_ROOM) {

            LAST_TARGET_ACTIONS.put(targetID, ENTERED_CHAT_ROOM);

            String targetName = TARGETS_NAMES.get(targetID);

            _NotificationManager.sendNotification(RELATIONS.get(targetID), _Config.APP_NAME_CAFE_GAME,
                    "Activity Reporter", targetName + " entered the chat room.");
        }
    }

    public static void setLeftChatRoom(int targetID) {

        if (RELATIONS.containsKey(targetID) && LAST_TARGET_ACTIONS.getOrDefault(targetID, 0) != LEFT_CHAT_ROOM) {

            LAST_TARGET_ACTIONS.put(targetID, LEFT_CHAT_ROOM);

            String targetName = TARGETS_NAMES.get(targetID);

            _NotificationManager.sendNotification(RELATIONS.get(targetID), _Config.APP_NAME_CAFE_GAME,
                    "Activity Reporter", targetName + " left the chat room.");
        }
    }
}