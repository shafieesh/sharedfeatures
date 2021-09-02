package com.chainedminds.utilities;

import com.chainedminds.BaseConfig;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
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
    public static final String SYSTEM_CHATROOM = "SYSTEM_CHATROOM";
    public static final String SYSTEM_LOTTERIES = "SYSTEM_LOTTERIES";

    private static final String FIELD_SECTION = "Section";
    private static final String FIELD_MESSAGE_ID = "MessageID";
    private static final String FIELD_LANGUAGE = "Language";
    private static final String FIELD_VALUE = "Value";

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final List<Message> MESSAGES = new ArrayList<>();

    public static void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName(TAG)
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .setTimingListener(task -> fetch())
                .startAndSchedule());
    }

    private static void fetch() {

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_MESSAGES;

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

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

        public static String LEVEL = "LEVEL";
        public static String YOU_HAVE_TOO_MANY_FRIENDS = "YOU_HAVE_TOO_MANY_FRIENDS";
        public static String TARGET_HAS_TOO_MANY_FRIENDS = "TARGET_HAS_TOO_MANY_FRIENDS";
        public static String YOU_HAVE_LEFT_TOO_MUCH = "YOU_HAVE_LEFT_TOO_MUCH";
        public static String NOT_ENOUGH_COINS = "NOT_ENOUGH_COINS";
        public static String NOT_ENOUGH_TICKETS = "NOT_ENOUGH_TICKETS";
        public static String INVALID_APPLICATION_ID = "INVALID_APPLICATION_ID";
        public static String TRY_LATER_FOR_MORE_VIDEOS = "TRY_LATER_FOR_MORE_VIDEOS";
        public static String YOU_GOT_COINS_AS_REWARD = "YOU_GOT_COINS_AS_REWARD";

        public static String SERVERS_ARE_NOT_AVAILABLE = "SERVERS_ARE_NOT_AVAILABLE";
        public static String MISSING_DATA = "MISSING_DATA";
        public static String MISSING_PERMISSION = "MISSING_PERMISSION";
        public static String INVALID_GAMER_TAG_OR_PASSWORD = "INVALID_GAMER_TAG_OR_PASSWORD";
        public static String SOMETHING_WENT_WRONG = "SOMETHING_WENT_WRONG";
        public static String SOMETHING_WENT_WRONG_TRY_AGAIN = "SOMETHING_WENT_WRONG_TRY_AGAIN";
        public static String TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS";

        public static String PREMIUM_IS_ALREADY_ACTIVE = "PREMIUM_IS_ALREADY_ACTIVE";

        public static String GAMER_TAG_HAS_REGISTERED_BEFORE = "GAMER_TAG_HAS_REGISTERED_BEFORE";
        public static String ILLEGAL_GAMER_TAG = "ILLEGAL_GAMER_TAG";
        public static String PAYMENT_INFO_IS_NOT_VALID = "PAYMENT_INFO_IS_NOT_VALID";
        public static String THANK_YOU_FOR_YOUR_PURCHASE = "THANK_YOU_FOR_YOUR_PURCHASE";
        public static String YOU_ARE_BLOCKED_FOR_SOME_TIMES = "YOU_ARE_BLOCKED_FOR_SOME_TIMES";
        public static String SUBSCRIPTION_CANCELED = "SUBSCRIPTION_CANCELED";
        public static String PASSWORD_IS_TOO_SHORT = "PASSWORD_IS_TOO_SHORT";
        public static String YOU_RECEIVED_A_REWARD = "YOU_RECEIVED_A_REWARD";
        public static String YOU_RECEIVED_REWARD_FOR_COMPLETING_PROFILE = "YOU_RECEIVED_REWARD_FOR_COMPLETING_PROFILE";
    }

    public static class Lobby {

        public static String NAME = "NAME";
        public static String SOMEONE_SAID = "SOMEONE_SAID";
        public static String YOU_HAVE_KICKED_FOR_NOT_PLAYING = "YOU_HAVE_KICKED_FOR_NOT_PLAYING";
        public static String GOT_ONE_MORE_MOVE = "GOT_ONE_MORE_MOVE";
        public static String GAME_ENDED_IN_DRAW = "GAME_ENDED_IN_DRAW";
        public static String SOMEONE_WON_THE_GAME = "SOMEONE_WON_THE_GAME";
        public static String YOU_WON_THE_GAME = "YOU_WON_THE_GAME";
        public static String SOMEONE_JOINED_THE_GAME = "SOMEONE_JOINED_THE_GAME";
        public static String SOMEONE_LEFT_THE_GAME = "SOMEONE_LEFT_THE_GAME";
        public static String YOU_LOST_CAUSE_YOU_DIDNT_PLAY = "YOU_LOST_CAUSE_YOU_DIDNT_PLAY";
        public static String YOU_WILL_LOSE_IF_YOU_DONT_PLAY_THE_NEXT_TIME = "YOU_WILL_LOSE_IF_YOU_DONT_PLAY_THE_NEXT_TIME";
        public static String YOU_CAN_ONLY_USE_IT_ONCE = "YOU_CAN_ONLY_USE_IT_ONCE";
        public static String YOU_DONT_HAVE_ENOUGH_COINS = "YOU_DONT_HAVE_ENOUGH_COINS";
        public static String GREET_IN_GAME_CHAT = "GREET_IN_GAME_CHAT";
        public static String TIME_REMAINING = "TIME_REMAINING";

        public static class ChatRoom {

            public static String LEVEL_UP_TO_5_TO_MESSAGE = "LEVEL_UP_TO_5_TO_MESSAGE";
            public static String YOU_ARE_MUTED_TILL = "YOU_ARE_MUTED_TILL";
            public static String SUPPORT = "#SUPPORT";
            public static String MOVE_TO_CHANNEL = "#MOVE_TO_CHANNEL";
            public static String CLEAR = "#CLEAR";
            public static String HELP = "#HELP";
            public static String MOVE_ALL_TO_CHANNEL = "#MOVE_ALL_TO_CHANNEL";
            public static String MUTE = "#MUTE";
            public static String SUDO = "#SUDO";
            public static String LOBBIES_INFO = "#LOBBIES_INFO";

            public static String GUIDE = "GUIDE";
            public static String EXITING_FROM_LOBBY = "EXITING_FROM_LOBBY";
            public static String JOINED_THE_LOBBY = "JOINED_THE_LOBBY";
            public static String YOUR_LAST_PVS = "YOUR_LAST_PVS";

            public static String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";

            public static String YOU_ARE_NOT_FRIENDS = "YOU_ARE_NOT_FRIENDS";
            public static String PLAYER_NOT_FOUND = "PLAYER_NOT_FOUND";

            public static String YOU_ARE_CURRENTLY_IN_LOBBY_X = "YOU_ARE_CURRENTLY_IN_LOBBY_X";
            public static String YOU_CANT_SWITCH_TO_THIS_LOBBY = "YOU_CANT_SWITCH_TO_THIS_LOBBY";
            public static String LOBBY_X_DOES_NOT_EXIST = "LOBBY_X_DOES_NOT_EXIST";
            public static String TOO_MANY_MESSAGES = "TOO_MANY_MESSAGES";
            public static String TOP_TEN_PLAYERS_ARE = "TOP_TEN_PLAYERS_ARE";
            public static String TOP_TEN_PLAYERS_ARE_PREMIUM = "TOP_TEN_PLAYERS_ARE_PREMIUM";
            public static String UPGRADE_TO_CAFEGAME = "UPGRADE_TO_CAFEGAME";
            public static String INTRODUCE_BLACK_LIST = "INTRODUCE_BLACK_LIST";
        }

        public static class DB {

            public static String GOT_ONE_MORE_MOVE = "GOT_ONE_MORE_MOVE";
        }

        public static class TF {

            public static String TAP_NUMBERS_IN_ASCENDING_ORDER = "TAP_NUMBERS_IN_ASCENDING_ORDER";
        }
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

    public static class Lotteries {

        public static final String INSUFFICIENT_COINS = "INSUFFICIENT_COINS";
        public static final String LOTTERY_IS_OVER = "LOTTERY_IS_OVER";
        public static final String YOU_HAVE_WON = "YOU_HAVE_WON";
        public static final String YOU_HAVE_WON_COINS = "YOU_HAVE_WON_COINS";
    }

    public static class Leagues {

        public static final String INSUFFICIENT_COINS = "INSUFFICIENT_COINS";
        public static final String ALREADY_PARTICIPATING = "ALREADY_PARTICIPATING";
        public static final String LEAGUE_IS_OVER = "LEAGUE_IS_OVER";
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