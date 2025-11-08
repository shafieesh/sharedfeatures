package com.chainedminds;

import com.chainedminds.api._API;
import okhttp3.Interceptor;

public class _Config {

    public static final int NOT_FOUND = -1;

    //-------------------------------------------------------------//

    public static final boolean USING_FEATURE_SUBSCRIPTION = false;
    public static final boolean USING_FEATURE_COIN = false;

    public static String FIREBASE_KEY;
    public static String FIREBASE_PROJECT_ID;

    //--------------------------------

    public static final int ONE_MINUTE = 1000 * 60;
    public static final int TWO_MINUTES = 2 * ONE_MINUTE;
    public static final int FIVE_MINUTES = 5 * ONE_MINUTE;
    public static final int TEN_MINUTES = 10 * ONE_MINUTE;
    public static final int FIFTEEN_MINUTES = 15 * ONE_MINUTE;
    public static final int TWENTY_MINUTES = 20 * ONE_MINUTE;
    public static final int THIRTY_MINUTES = 30 * ONE_MINUTE;
    public static final int SIXTY_MINUTES = 60 * ONE_MINUTE;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_MONTH = 30 * ONE_DAY;
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    //--------------------------------

    private static final int ONE_K = 1000;
    private static final int ONE_M = 1000 * ONE_K;
    public static final int ONE_B = 1000 * ONE_M;

    //--------------------------------------------------------------//
    //-----------------------------LOBBY----------------------------//

    public static final int RESPONSE_OK = 100;
    public static final int RESPONSE_NOK = -100;

    public static final String MARKET_CAFEBAZAAR = "CafeBazaar";
    public static final String MARKET_GOOGLE_PLAY = "GooglePlay";
    public static final String MARKET_APP_STORE = "AppStore";
    public static final String MARKET_MYKET = "Myket";
    public static final String MARKET_VADA = "Vada";
    public static final String MARKET_ROYAL = "Royal";
    public static final String MARKET_TOOSKA = "Tooska";
    public static final String MARKET_IRANAPPS = "IranApps";

    public static final String PAYMENT_OPTION_IAB = "IAB";
    public static final String PAYMENT_OPTION_IPG = "IPG";
    public static final String PAYMENT_OPTION_VAS = "VAS";
    public static final String LANGUAGE_FA = "FA";
    public static final String LANGUAGE_EN = "EN";
    public static final String LANGUAGE_DEFAULT = LANGUAGE_EN;
    public static final String[] ADMINS = {};
    public static final String APP_NAME_CAFE_GAME = "CafeGame";

    public static String DATABASE_NAME;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;
    public static int SOCKET_TIME_OUT_API_PIPE = 15 * 1000;
    public static int SOCKET_TIME_OUT_MAIN_PIPE = 60 * 1000;
    public static final int MEDIA_STRING = 1;
    public static final int MEDIA_IMAGE = 2;
    public static final int MEDIA_AUDIO = 3;
    public static final int MEDIA_APK = 4;

    public static class OkHTTP {

        public boolean OKHTTP_FOLLOW_REDIRECTS = false;
        public int OKHTTP_DISPATCHER_MAX_REQUESTS = 250;
        public int OKHTTP_DISPATCHER_MAX_REQUEST_PER_HOST = 50;
        public int OKHTTP_CONNECTION_POOL_IDLE_CONNECTIONS = 50;
        public int OKHTTP_CONNECTION_POOL_KEEP_ALIVE_DURATION = 60;
        public long OKHTTP_CALL_TIMEOUT = 30_000;
        public long OKHTTP_CONNECT_TIMEOUT = 10_000;
        public long OKHTTP_READ_TIMEOUT = 10_000;
        public long OKHTTP_WRITE_TIMEOUT = 10_000;
        public String OKHTTP_PROXY;
        public String OKHTTP_PROXY_ADDRESS;
        public int OKHTTP_PROXY_PORT;
        public Interceptor OKHTTP_NETWORK_INTERCEPTOR;
    }

    public static int BRUTE_FORCE_ALLOWED_ATTEMPTS = 7;
    public static int BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER = FIFTEEN_MINUTES;

    public static int CACHED_USERS_INFO_REFRESH_RATE = 5;

    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_WHEEL_OF_FORTUNE_PRIZES = "wheel_of_fortune_prizes";
    public static String TABLE_WHEEL_OF_FORTUNE_PRIZES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_SPINS = "spins";
    public static String TABLE_SPINS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LOTTERY_TICKETS = "tickets";
    public static String TABLE_LOTTERY_TICKETS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LEAGUES = "leagues";
    public static String TABLE_LEAGUES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LEAGUE_GAMES = "league_games";
    public static String TABLE_LEAGUE_GAMES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_PRIZES = "prizes";
    public static String TABLE_PRIZES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ERRORS_SERVER = "errors_server";
    public static String TABLE_ERRORS_SERVER;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_PRODUCTS = "products";
    public static String TABLE_PRODUCTS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_TASKS_HISTORY = "tasks_history";
    public static String TABLE_TASKS_HISTORY;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_VERSION_CONTROL = "version_control";
    public static String TABLE_VERSION_CONTROL;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LOTTERIES = "lotteries";
    public static String TABLE_LOTTERIES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_USERS = "users";
    public static String TABLE_USERS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LEAGUE_PRIZES = "league_prizes";
    public static String TABLE_LEAGUE_PRIZES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_IAB_TRANSACTIONS = "transactions_iab";
    public static String TABLE_IAB_TRANSACTIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACTIVITY_FINDER = "activity_finder";
    public static String TABLE_ACTIVITY_FINDER;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_FRIENDS_LIST = "friends_list";
    public static String TABLE_FRIENDS_LIST;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_QUIZ_WARS_QUESTIONS_TEMP = "quiz_wars_questions_temp";
    public static String TABLE_QUIZ_WARS_QUESTIONS_TEMP;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_QUIZ_WARS_QUESTIONS = "quiz_wars_questions";
    public static String TABLE_QUIZ_WARS_QUESTIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ROBOT_MESSAGES = "robot_messages";
    public static String TABLE_ROBOT_MESSAGES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_SUPPORT_SESSIONS = "support_sessions";
    public static String TABLE_SUPPORT_SESSIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_SUPPORT_MESSAGES = "support_messages";
    public static String TABLE_SUPPORT_MESSAGES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_NEWS = "content_news";
    public static String TABLE_NEWS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS_TITLES_PENDING = "accounts_titles_pending";
    public static String TABLE_ACCOUNTS_TITLES_PENDING;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_PURCHASES_IAB_PRODUCTS = "purchases_iab_products";
    public static String TABLE_PURCHASES_IAB_PRODUCTS;
    //----------------------------------
    public static String TABLE_NAME_PURCHASES_IAB_SUBSCRIPTIONS = "purchases_iab_subscriptions";
    public static String TABLE_PURCHASES_IAB_SUBSCRIPTIONS;
    //----------------------------------
    public static String TABLE_NAME_PURCHASES_IPG = "purchases_ipg";
    public static String TABLE_PURCHASES_IPG;
    //----------------------------------
    public static String TABLE_NAME_PURCHASES_IPG_PRODUCTS = "purchases_ipg_products";
    public static String TABLE_PURCHASES_IPG_PRODUCTS;
    //----------------------------------
    public static String TABLE_NAME_PURCHASES_IPG_PASARGAD = "purchases_ipg_pasargad";
    public static String TABLE_PURCHASES_IPG_PASARGAD;
    //----------------------------------
    public static String TABLE_NAME_PURCHASES_IPG_ZARINPAL = "purchases_ipg_zarinpal";
    public static String TABLE_PURCHASES_IPG_ZARINPAL;
    //----------------------------------
    public static String TABLE_NAME_PURCHASES_IPG_SUBSCRIPTIONS = "purchases_ipg_subscriptions";
    public static String TABLE_PURCHASES_IPG_SUBSCRIPTIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LOG_REQUESTS = "log_requests";
    public static String TABLE_LOG_REQUESTS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_TASKS = "tasks";
    public static String TABLE_TASKS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_DEVICES_PROPERTIES = "devices_properties";
    public static String TABLE_DEVICES_PROPERTIES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LEAGUE_TICKETS = "league_tickets";
    public static String TABLE_LEAGUE_TICKETS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_USERS_OWNED_CUSTOMIZATIONS = "users_owned_customizations";
    public static String TABLE_USERS_OWNED_CUSTOMIZATIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_BACKEND_LOGS = "log_backend";
    public static String TABLE_BACKEND_LOGS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_CRASH_REPORTS = "crash_reports";
    public static String TABLE_CRASH_REPORTS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_MAPPING = "mapping";
    public static String TABLE_MAPPING;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_TRANSACTIONS_INTERNAL = "transactions_internal";
    public static String TABLE_TRANSACTIONS_INTERNAL;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS = "accounts";
    public static String TABLE_ACCOUNTS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_AUTH_USERNAME = "auth_username";
    public static String TABLE_AUTH_USERNAME;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS_SESSIONS = "accounts_sessions";
    public static String TABLE_ACCOUNTS_SESSIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS_PROFILE = "accounts_profiles";
    public static String TABLE_ACCOUNTS_PROFILES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_PERMISSIONS = "permissions";
    public static String TABLE_PERMISSIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS_PERMISSIONS = "accounts_permissions";
    public static String TABLE_ACCOUNTS_PERMISSIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS_CUSTOMIZATIONS = "accounts_customizations";
    public static String TABLE_ACCOUNTS_CUSTOMIZATIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_USER_CHANNELS = "user_channels";
    public static String TABLE_USER_CHANNELS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ACCOUNTS_TITLES = "accounts_titles";
    public static String TABLE_ACCOUNTS_TITLES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ASSETS = "assets";
    public static String TABLE_ASSETS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_ASSETS_GRADIENTS = "assets_gradients";
    public static String TABLE_ASSETS_GRADIENTS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_BLACK_LIST = "black_list";
    public static String TABLE_BLACK_LIST;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LEVELS = "levels";
    public static String TABLE_LEVELS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_MESSAGES = "messages";
    public static String TABLE_MESSAGES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_GAME_MESSAGES = "game_messages";
    public static String TABLE_GAME_MESSAGES;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_CHATS_PRIVATE = "chats_private";
    public static String TABLE_CHATS_PRIVATE;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_GAME_SESSIONS = "game_sessions";
    public static String TABLE_GAME_SESSIONS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_DYNAMIC_CONFIGS = "configs";
    public static String TABLE_DYNAMIC_CONFIGS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LOGS = "logs";
    public static String TABLE_LOGS;
    //----------------------------------------------------------------------------------------------------------------//
    public static String TABLE_NAME_LEADERBOARD_DAILY = "leaderboard_daily";
    public static String TABLE_LEADERBOARD_DAILY;

    //-------------------------------------------------------------//

    public static String ENGINE_NAME;

    public static int SERVER_PORT_MAIN;
    public static int SERVER_PORT_TELNET;

    public static int DEFAULT_TIMEOUT = 30;

    //-------------------------------------------------------------//

    public static boolean DEBUG_MODE = false;
    public static String DATABASE_URL;

    //-------------------------------------------------------------//

    public static void config() {

        TABLE_GAME_SESSIONS = DATABASE_NAME + "." + TABLE_NAME_GAME_SESSIONS;
        TABLE_CHATS_PRIVATE = DATABASE_NAME + "." + TABLE_NAME_CHATS_PRIVATE;
        TABLE_GAME_MESSAGES = DATABASE_NAME + "." + TABLE_NAME_GAME_MESSAGES;
        TABLE_MESSAGES = DATABASE_NAME + "." + TABLE_NAME_MESSAGES;
        TABLE_LEVELS = DATABASE_NAME + "." + TABLE_NAME_LEVELS;
        TABLE_BLACK_LIST = DATABASE_NAME + "." + TABLE_NAME_BLACK_LIST;
        TABLE_ASSETS_GRADIENTS = DATABASE_NAME + "." + TABLE_NAME_ASSETS_GRADIENTS;
        TABLE_ASSETS = DATABASE_NAME + "." + TABLE_NAME_ASSETS;
        TABLE_ACCOUNTS_TITLES = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS_TITLES;
        TABLE_USER_CHANNELS = DATABASE_NAME + "." + TABLE_NAME_USER_CHANNELS;
        TABLE_LOG_REQUESTS = DATABASE_NAME + "." + TABLE_NAME_LOG_REQUESTS;
        TABLE_CRASH_REPORTS = DATABASE_NAME + "." + TABLE_NAME_CRASH_REPORTS;
        TABLE_WHEEL_OF_FORTUNE_PRIZES = DATABASE_NAME + "." + TABLE_NAME_WHEEL_OF_FORTUNE_PRIZES;
        TABLE_LOTTERY_TICKETS = DATABASE_NAME + "." + TABLE_NAME_LOTTERY_TICKETS;
        TABLE_LEAGUES = DATABASE_NAME + "." + TABLE_NAME_LEAGUES;
        TABLE_LEAGUE_GAMES = DATABASE_NAME + "." + TABLE_NAME_LEAGUE_GAMES;
        TABLE_PRIZES = DATABASE_NAME + "." + TABLE_NAME_PRIZES;
        TABLE_ERRORS_SERVER = DATABASE_NAME + "." + TABLE_NAME_ERRORS_SERVER;
        TABLE_PRODUCTS = DATABASE_NAME + "." + TABLE_NAME_PRODUCTS;
        TABLE_TASKS_HISTORY = DATABASE_NAME + "." + TABLE_NAME_TASKS_HISTORY;
        TABLE_VERSION_CONTROL = DATABASE_NAME + "." + TABLE_NAME_VERSION_CONTROL;
        TABLE_LOTTERIES = DATABASE_NAME + "." + TABLE_NAME_LOTTERIES;
        TABLE_USERS = DATABASE_NAME + "." + TABLE_NAME_USERS;
        TABLE_LEAGUE_PRIZES = DATABASE_NAME + "." + TABLE_NAME_LEAGUE_PRIZES;
        TABLE_IAB_TRANSACTIONS = DATABASE_NAME + "." + TABLE_NAME_IAB_TRANSACTIONS;
        TABLE_ACTIVITY_FINDER = DATABASE_NAME + "." + TABLE_NAME_ACTIVITY_FINDER;
        TABLE_FRIENDS_LIST = DATABASE_NAME + "." + TABLE_NAME_FRIENDS_LIST;
        TABLE_QUIZ_WARS_QUESTIONS_TEMP = DATABASE_NAME + "." + TABLE_NAME_QUIZ_WARS_QUESTIONS_TEMP;
        TABLE_QUIZ_WARS_QUESTIONS = DATABASE_NAME + "." + TABLE_NAME_QUIZ_WARS_QUESTIONS;
        TABLE_ROBOT_MESSAGES = DATABASE_NAME + "." + TABLE_NAME_ROBOT_MESSAGES;
        TABLE_SUPPORT_SESSIONS = DATABASE_NAME + "." + TABLE_NAME_SUPPORT_SESSIONS;
        TABLE_SUPPORT_MESSAGES = DATABASE_NAME + "." + TABLE_NAME_SUPPORT_MESSAGES;
        TABLE_NEWS = DATABASE_NAME + "." + TABLE_NAME_NEWS;
        TABLE_PURCHASES_IAB_PRODUCTS = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IAB_PRODUCTS;
        TABLE_PURCHASES_IAB_SUBSCRIPTIONS = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IAB_SUBSCRIPTIONS;
        TABLE_PURCHASES_IPG = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IPG;
        TABLE_PURCHASES_IPG_PRODUCTS = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IPG_PRODUCTS;
        TABLE_PURCHASES_IPG_PASARGAD = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IPG_PASARGAD;
        TABLE_PURCHASES_IPG_ZARINPAL = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IPG_ZARINPAL;
        TABLE_PURCHASES_IPG_SUBSCRIPTIONS = DATABASE_NAME + "." + TABLE_NAME_PURCHASES_IPG_SUBSCRIPTIONS;
        TABLE_ACCOUNTS_TITLES_PENDING = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS_TITLES_PENDING;
        TABLE_TASKS = DATABASE_NAME + "." + TABLE_NAME_TASKS;
        TABLE_DEVICES_PROPERTIES = DATABASE_NAME + "." + TABLE_NAME_DEVICES_PROPERTIES;
        TABLE_LEAGUE_TICKETS = DATABASE_NAME + "." + TABLE_NAME_LEAGUE_TICKETS;
        TABLE_USERS_OWNED_CUSTOMIZATIONS = DATABASE_NAME + "." + TABLE_NAME_USERS_OWNED_CUSTOMIZATIONS;
        TABLE_ACCOUNTS_CUSTOMIZATIONS = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS_CUSTOMIZATIONS;
        TABLE_PERMISSIONS = DATABASE_NAME + "." + TABLE_NAME_PERMISSIONS;
        TABLE_ACCOUNTS_PERMISSIONS = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS_PERMISSIONS;
        TABLE_ACCOUNTS_PROFILES = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS_PROFILE;
        TABLE_ACCOUNTS = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS;
        TABLE_AUTH_USERNAME = DATABASE_NAME + "." + TABLE_NAME_AUTH_USERNAME;
        TABLE_ACCOUNTS_SESSIONS = DATABASE_NAME + "." + TABLE_NAME_ACCOUNTS_SESSIONS;
        TABLE_TRANSACTIONS_INTERNAL = DATABASE_NAME + "." + TABLE_NAME_TRANSACTIONS_INTERNAL;
        TABLE_MAPPING = DATABASE_NAME + "." + TABLE_NAME_MAPPING;
        TABLE_BACKEND_LOGS = DATABASE_NAME + "." + TABLE_NAME_BACKEND_LOGS;
        TABLE_SPINS = DATABASE_NAME + "." + TABLE_NAME_SPINS;
        TABLE_DYNAMIC_CONFIGS = DATABASE_NAME + "." + TABLE_NAME_DYNAMIC_CONFIGS;
        TABLE_LOGS = DATABASE_NAME + "." + TABLE_NAME_LOGS;

        TABLE_LEADERBOARD_DAILY = DATABASE_NAME + "." + TABLE_NAME_LEADERBOARD_DAILY;

        _API.instance().init(new OkHTTP());
    }
}