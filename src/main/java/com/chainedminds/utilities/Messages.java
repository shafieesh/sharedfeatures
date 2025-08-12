package com.chainedminds.utilities;

import com.chainedminds._Config;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Messages {

    protected static final String TAG = Messages.class.getSimpleName();

    public static final String ACTIVITIES = "ACTIVITIES";

    private static final String FIELD_SECTION = "Section";
    private static final String FIELD_MESSAGE_ID = "MessageID";
    private static final String FIELD_LANGUAGE = "Language";
    private static final String FIELD_VALUE = "Value";

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

                    message.section = resultSet.getString(FIELD_SECTION);
                    message.messageID = resultSet.getString(FIELD_MESSAGE_ID);
                    message.language = resultSet.getString(FIELD_LANGUAGE);
                    message.value = resultSet.getString(FIELD_VALUE);

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

    public static String get(String section, String messageID, String language, String value) {

        return get(section, messageID, language, new String[]{value});
    }

    public static String get(String section, String messageID, String language, String[] values) {

        String message = get(section, messageID, language);

        message = fulfill(message, values);

        message = fixDirections(message, language);

        return message;
    }

    public static String get(String section, String messageID, String language) {

        String isoLanguage = language.toUpperCase();

        AtomicReference<String> translatedMessage = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Message message : MESSAGES) {

                if (message.section.equals(section) &&
                        message.messageID.equals(messageID) &&
                        message.language.equals(isoLanguage)) {

                    translatedMessage.set(message.value);

                    break;
                }
            }
        });

        if (translatedMessage.get() == null) {

            translatedMessage.set("MESSAGE " + section + "-" + messageID + " IS NOT DEFINED");
        }

        return translatedMessage.get();
    }

    public static String fulfill(String message, String value) {

        return fulfill(message, new String[]{value});
    }

    public static String fulfill(String message, String[] values) {

        for (int index = 0; index < values.length; index++) {

            message = message.replace("$" + index, values[index]);
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

        String section;
        String messageID;
        String language;
        String value;
    }

    public static class General {

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

    public static class Notification {

        public static final String TOUCH_HERE_TO_OPEN = "TOUCH_HERE_TO_OPEN";

        public static class Friendship {

            public static final String NEW_FRIEND_REQUEST = "NEW_FRIEND_REQUEST";
            public static final String FRIEND_BECAME_ONLINE = "FRIEND_BECAME_ONLINE";
            public static final String ACCEPTED_YOUR_FRIENDSHIP = "ACCEPTED_YOUR_FRIENDSHIP";

            public static final String TAG_NEW_FRIEND_REQUEST = "friendship_new_friendship_request";
            public static final String TAG_FRIEND_BECAME_ONLINE = "friendship_friend_became_online";
            public static final String TAG_ACCEPTED_YOUR_FRIENDSHIP = "friendship_accepted_your_friendship";
        }
    }

    public static class Friendship {

        public static final String GAMERTAG_IS_NOW_ONLINE = "GAMERTAG_IS_NOW_ONLINE";
        public static final String GAMERTAG_IS_ONLINE = "GAMERTAG_IS_ONLINE";
        public static final String X_PLAYERS_ARE_ONLINE = "X_PLAYERS_ARE_ONLINE";
    }

    public static class Activities {

        public static final String PLAYING_DB = "PLAYING_DB";
        public static final String PLAYING_C4 = "PLAYING_C4";
        public static final String PLAYING_TF = "PLAYING_TF";
        public static final String PLAYING_BB = "PLAYING_BB";
        public static final String PLAYING_MCH = "PLAYING_MCH";
        public static final String PLAYING_QW = "PLAYING_QW";
        public static final String PLAYING_FF = "PLAYING_FF";
        public static final String PLAYING_WE = "PLAYING_WE";

        public static final String ONLINE = "ONLINE";

        public static final String WAS_ONLINE_YEARS_AGO = "WAS_ONLINE_YEARS_AGO";
        public static final String WAS_ONLINE_MONTHS_AGO = "WAS_ONLINE_MONTHS_AGO";
        public static final String WAS_ONLINE_DAYS_AGO = "WAS_ONLINE_DAYS_AGO";
        public static final String WAS_ONLINE_HOURS_AGO = "WAS_ONLINE_HOURS_AGO";
        public static final String WAS_ONLINE_MINUTES_AGO = "WAS_ONLINE_MINUTES_AGO";
    }
}