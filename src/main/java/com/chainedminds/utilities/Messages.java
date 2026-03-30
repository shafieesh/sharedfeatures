package com.chainedminds.utilities;

import com.chainedminds._Config;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Messages {

    protected static final String TAG = Messages.class.getSimpleName();

    private static final String FIELD_ID = "ID";
    private static final String FIELD_LANGUAGE = "Language";
    private static final String FIELD_MESSAGE = "Message";

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final List<Message> MESSAGES = new ArrayList<>();

    public static void start() {

        Task.add(Task.Data.build()
                .setName(TAG)
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .onEachCycle(Messages::fetch)
                .runNow()
                .schedule());
    }

    private static void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_MESSAGES;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<Message> messages = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    Message message = new Message();
                    message.id = resultSet.getString(FIELD_ID);
                    message.language = resultSet.getString(FIELD_LANGUAGE);
                    message.message = resultSet.getString(FIELD_MESSAGE);

                    messages.add(message);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        MESSAGES.clear();

                        MESSAGES.addAll(messages);
                    });
                }
            }
        });
    }

    public static String get(String messageID, String language) {

        String isoLanguage = language.toUpperCase();

        AtomicReference<String> messageHolder = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Message message : MESSAGES) {

                if (message.id.equals(messageID) && message.language.equals(isoLanguage)) {

                    messageHolder.set(message.message);
                    break;
                }
            }
        });

        if (messageHolder.get() == null) {

            messageHolder.set("MESSAGE " + messageID + " IS NOT DEFINED");
        }

        return messageHolder.get();
    }

    public static String get(String messageID, String language, String value) {

        return get(messageID, language, Collections.singletonList(value));
    }

    public static String get(String messageID, String language, String... values) {

        String message = get(messageID, language);

        message = fulfill(message, Arrays.asList(values));

        message = fixDirections(message, language);

        return message;
    }

    public static String get(String messageID, String language, List<String> values) {

        String message = get(messageID, language);

        message = fulfill(message, values);

        message = fixDirections(message, language);

        return message;
    }

    public static String fulfill(String message, List<String> values) {

        for (int index = 0; index < values.size(); index++) {

            String value = values.get(index);

            message = message.replace("$" + index, value);
        }

        return message;
    }

    public static String fixDirections(String message, String language) {

        language = language.toUpperCase();

        if (isLanguageRTL(language)) {

            message = "\u202B" + message + "\u202C";

        } else {

            message = "\u202A" + message + "\u202C";
        }

        return message;
    }

    private static boolean isLanguageRTL(String language) {

        return "FA".equals(language) || "AR".equals(language);
    }

    private static class Message {

        String id;
        String language;
        String message;
    }

    public static String YOU_HAVE_TOO_MANY_FRIENDS = "YOU_HAVE_TOO_MANY_FRIENDS";
    public static String TARGET_HAS_TOO_MANY_FRIENDS = "TARGET_HAS_TOO_MANY_FRIENDS";
    public static String SERVERS_ARE_NOT_AVAILABLE = "SERVERS_ARE_NOT_AVAILABLE";
    public static String MISSING_DATA = "MISSING_DATA";
    public static String MISSING_PERMISSION = "MISSING_PERMISSION";
    public static String USERNAME_IS_TOO_SHORT = "USERNAME_IS_TOO_SHORT";
    public static String PASSWORD_IS_TOO_SHORT = "PASSWORD_IS_TOO_SHORT";
    public static String INVALID_USERNAME_OR_PASSWORD = "INVALID_USERNAME_OR_PASSWORD";
    public static String SOMETHING_WENT_WRONG = "SOMETHING_WENT_WRONG";
    public static String SOMETHING_WENT_WRONG_TRY_AGAIN = "SOMETHING_WENT_WRONG_TRY_AGAIN";
    public static String TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS";
    public static String CREDENTIAL_EXPIRED = "CREDENTIAL_EXPIRED";
    public static String ACCOUNT_DEACTIVATED = "ACCOUNT_DEACTIVATED";
    public static String USERNAME_HAS_REGISTERED_BEFORE = "USERNAME_HAS_REGISTERED_BEFORE";
}