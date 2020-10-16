package com.chainedminds.utilities;

import com.chainedminds.BaseConfig;
import com.chainedminds.utilities.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameMessages {

    private static final String TAG = GameMessages.class.getSimpleName();

    private static final String FIELD_MESSAGE_ID = "MessageID";
    private static final String FIELD_LANGUAGE = "Language";
    private static final String FIELD_GAME_NAME = "GameName";
    private static final String FIELD_MESSAGE = "Message";

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final List<GameMessage> GAME_MESSAGES = new ArrayList<>();

    public static void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName(TAG)
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .setTimingListener(task -> fetch())
                .startAndSchedule());
    }

    private static void fetch() {

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_GAME_MESSAGES;

        DatabaseHelper.query(TAG, selectStatement, resultSet -> {

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                GAME_MESSAGES.clear();

                while (resultSet.next()) {

                    GameMessage gameMessage = new GameMessage();

                    gameMessage.gameName = resultSet.getString(FIELD_GAME_NAME);
                    gameMessage.messageID = resultSet.getInt(FIELD_MESSAGE_ID);
                    gameMessage.language = resultSet.getString(FIELD_LANGUAGE);
                    gameMessage.message = resultSet.getString(FIELD_MESSAGE);

                    GAME_MESSAGES.add(gameMessage);
                }
            });
        });
    }

    public static String get(String gameName, String language, int messageID) {

        AtomicReference<String> translatedMessage = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (GameMessage gameMessage : GAME_MESSAGES) {

                if (gameMessage.gameName.equals(gameName) &&
                        gameMessage.messageID == messageID &&
                        gameMessage.language.equals(language)) {

                    translatedMessage.set(gameMessage.message);

                    break;
                }
            }
        });

        return translatedMessage.get();
    }

    public static String[] getList(String gameName, String language) {

        Map<Integer, String> messages = new TreeMap<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (GameMessage gameMessage : GAME_MESSAGES) {

                if (gameMessage.gameName.equals(gameName) &&
                        gameMessage.language.equals(language)) {

                    messages.put(gameMessage.messageID, gameMessage.message);
                }
            }
        });

        return messages.values().toArray(new String[0]);
    }

    private static class GameMessage {

        String gameName;
        int messageID;
        String language;
        String message;
    }
}