package com.chainedminds;

public class BaseCodes {

    public static final int NOT_FOUND = -1;
    public static final int UNDEFINED = NOT_FOUND;
    public static final int NOT_DEFINED = NOT_FOUND;

    public static final int RESPONSE_OK_CHANGE_UUID = 201;
    public static final int RESPONSE_OK_SYNC_INFO = 200;
    public static final int RESPONSE_OK = 100;
    public static final int RESPONSE_NOK = -100;
    public static final int RESPONSE_NOK_SERVER_IS_DOWN = -200;
    public static final int RESPONSE_NOK_INVALID_APPLICATION_ID = -201;

    public static final int RESPONSE_IS_NOT_REGISTERED = -100001;
    public static final int RESPONSE_CREDENTIAL_EXPIRED = -100002;
    public static final int RESPONSE_IS_REGISTERED_BEFORE = -100003;
    public static final int RESPONSE_INVALID_GAMER_TAG_OR_PASSWORD = -100004;

    public static final int REQUEST_BASIC_DATA = 1;
    public static final int REQUEST_FILE_INFO = 2;
    public static final int REQUEST_FILE_BYTES = 3;
    public static final int REQUEST_FILE_HASH = 4;

    public static final int REQUEST_GET_LEAGUE_BANNER = 1;
    public static final int REQUEST_GET_LEAGUE_PRIZE = 2;

    public static final int REQUEST_EMPTY = 0;
    public static final int REQUEST_GET_SYSTEM_STATUS = 1;
    public static final int REQUEST_GET_LATEST_UPDATE_INFO = 1000;
    public static final int REQUEST_OPEN_PAGE = 1001;
    public static final int REQUEST_SHUTDOWN_GAME_SERVER = 1002;
    public static final int REQUEST_SEARCH_USERNAMES = 1003;
    public static final int REQUEST_GET_WEEKLY_TOP_PLAYERS = 1004;
    public static final int REQUEST_GET_DAILY_TOP_PLAYERS = 1005;
    public static final int REQUEST_GET_TOTAL_ACTIVE_PLAYERS = 1006;
    public static final int REQUEST_PREPARE_DOTS_AND_BOXES_PLAYER = 1007;
    public static final int REQUEST_AUTHENTICATE_USER = 1008;
    public static final int REQUEST_AUTHENTICATE_USER_SESSION = 1009;
    public static final int REQUEST_REGISTER_USER = 1010;
    //public static final int REQUEST_____ = 1011;
    public static final int REQUEST_GET_WEEKLY_TOP_PLAYERS_2 = 1012;
    public static final int REQUEST_GET_DAILY_TOP_PLAYERS_2 = 1013;
    public static final int REQUEST_GET_ACTIVE_PLAYERS = 1014;
    public static final int REQUEST_SET_PASSWORD = 1015;
    public static final int REQUEST_PREPARE_CONNECT_4_PLAYER = 1016;
    public static final int REQUEST_PREPARE_TAP_FAST_PLAYER = 1017;
    public static final int REQUEST_GET_PLAYER_PROFILE = 1018;
    public static final int REQUEST_UPDATE_MY_PROFILE = 1019;
    public static final int REQUEST_SET_PHONE_NUMBER = 1020;
    public static final int REQUEST_SET_EMAIL = 1021;
    public static final int REQUEST_PREPARE_CHAT_ROOM_PLAYER = 1022;
    @Deprecated
    public static final int REQUEST_SET_NAME_DEPRECATED = 1023;
    public static final int REQUEST_SET_NAME = 1024;
    public static final int REQUEST_SET_BIO = 1025;
    public static final int REQUEST_REMOVE_PLAYER = 1026;
    public static final int REQUEST_GET_AVAILABLE_SERVERS = 1027;
    public static final int REQUEST_PREPARE_MENSCH_PLAYER = 1028;
    public static final int REQUEST_SAVE_SCORE = 1029;
    public static final int REQUEST_PREPARE_BATTLE_BRAINS_PLAYER = 1030;
    public static final int REQUEST_GET_CHAT_ROOM_ONLINE_PLAYERS = 1031;
    public static final int REQUEST_SEND_CR_MESSAGE = 1032;
    public static final int REQUEST_WARN_PLAYER = 1033;
    public static final int REQUEST_MUTE_PLAYER = 1034;
    public static final int REQUEST_ADD_QUIZ_WARS_QUESTION = 1035;
    public static final int REQUEST_GET_CHATROOM_CUSTOMIZATIONS = 1036;
    public static final int REQUEST_SET_CHATROOM_CUSTOMIZATIONS = 1037;
    public static final int REQUEST_ADD_APPROVED_QUIZ_WARS_QUESTION = 1038;
    public static final int REQUEST_GET_PENDING_QUIZ_WARS_QUESTIONS = 1039;
    public static final int REQUEST_DISAPPROVE_QUIZ_WARS_QUESTION = 1040;
    public static final int REQUEST_GET_ASSETS_DATA = 1041;
    public static final int REQUEST_DOWNLOAD_ASSET = 1042;
    public static final int REQUEST_GET_GAME_STATISTICS = 1043;
    public static final int REQUEST_PREPARE_QUIZ_WARS_PLAYER = 1044;
    public static final int REQUEST_PREPARE_FLUFFIA_PLAYER = 1045;
    public static final int REQUEST_OPEN_CUSTOMIZATIONS = 1046;
    public static final int REQUEST_BROADCAST_MESSAGE = 1047;
    public static final int REQUEST_PREPARE_PRIVATE_MATCH_PLAYER = 1048;
    public static final int REQUEST_GET_WHEEL_OF_FORTUNE = 1049;
    public static final int REQUEST_SPIN_WHEEL_OF_FORTUNE = 1050;
    public static final int REQUEST_GET_PAWN_CUSTOMIZATIONS = 1051;
    public static final int REQUEST_GET_PRIVATE_MESSAGES = 1052;
    public static final int REQUEST_SYNCHRONIZE_USER_INFO = 1053;
    public static final int REQUEST_GET_GAME_ENTRY_FEE = 1054;
    public static final int REQUEST_BUY_GAME_CUSTOMIZATION = 1055;
    public static final int REQUEST_GET_VIDEO_AD_AVAILABILITY = 1056;
    public static final int REQUEST_ADD_VIDEO_AD = 1057;
    public static final int REQUEST_GET_PENDING_TITLES = 1058;
    public static final int REQUEST_PREPARE_WORD_EATER_PLAYER = 1059;
    public static final int REQUEST_GET_CHAT_ROOM_RULES = 1060;
    public static final int REQUEST_GET_COIN_CHARGE_SETTINGS = 1061;
    public static final int REQUEST_GET_ENTRANCE_PERMISSION = 1062;
    public static final int REQUEST_REGISTER_GUEST_USER = 1063;
    //public static final int REQUEST_COMPLETE_ACCOUNT_INFO = 1064;
    public static final int REQUEST_PREPARE_MENSCH_2_PLAYER = 1065;

    public static final int REQUEST_SYNC_GAME_SETTINGS = 2000;
    public static final int REQUEST_SYNC_GAME_WINNER = 2001;
    public static final int REQUEST_RECOVER_MESSAGES = 2002;

    public static final int RESPONSE_LOW_POINTS = 1000;

    public static final int REQUEST_GET_MY_LOTTERIES = 20000;
    public static final int REQUEST_GET_LOTTERY_PRIZES = 20001;
    public static final int REQUEST_GET_ACTIVE_LOTTERY = 20002;
    public static final int REQUEST_GET_MY_PURCHASED_TICKETS = 20003;
    public static final int REQUEST_PURCHASE_TICKET = 20004;
    public static final int REQUEST_GET_MY_PRIZE_INFO = 20005;
    public static final int REQUEST_SET_MY_PRIZE_PAYLOAD = 20006;
    public static final int REQUEST_OPEN_LOTTERY = 20007;

    public static final int RESPONSE_INSUFFICIENT_COINS = -20000;
    public static final int RESPONSE_LOTTERY_IS_OVER = -20001;

    public static final int REQUEST_GET_CURRENT_LEAGUE = 30000;
    public static final int REQUEST_GET_LEAGUE_LEADERBOARD = 30001;
    public static final int REQUEST_PURCHASE_LEAGUE_TICKET = 30002;
    public static final int REQUEST_GET_LEAGUE_PRIZES = 30003;
    public static final int REQUEST_PLAY_IN_LEAGUE = 30004;
    public static final int REQUEST_OPEN_LEAGUE = 30005;

    public static final int RESPONSE_LEAGUE_IS_OVER = -30001;
    public static final int RESPONSE_ALREADY_PARTICIPATING = -30002;

    public static final int REQUEST_GET_STORE_PRODUCTS = 40000;
    public static final int REQUEST_REPORT_PURCHASE = 40001;
    public static final int REQUEST_CANCEL_SUBSCRIPTION = 40002;
    public static final int REQUEST_ADD_PREPARE_PAYMENT = 40003;
    public static final int REQUEST_REPORT_IPG_TRANSACTION = 40004;

    public static final int REQUEST_GET_FRIENDS_LIST = 50000;
    public static final int REQUEST_ADD_FRIEND = 50001;
    public static final int REQUEST_ACCEPT_FRIEND = 50002;
    public static final int REQUEST_REMOVE_FRIEND = 50003;
    public static final int REQUEST_BLOCK_FRIEND = 50004;
    public static final int REQUEST_UNBLOCK_FRIEND = 50005;
    public static final int REQUEST_REJECT_FRIEND = 50006;

    public static final int REQUEST_GET_NEWS_LIST = 60000;
    public static final int REQUEST_GET_NEWS_BY_ID = 60001;
    public static final int REQUEST_GET_NEWS_CONTENT = 60002;
    public static final int REQUEST_GET_NEWS_INDEX = 60003;
    public static final int REQUEST_OPEN_NEWS_LIST = 60004;
    public static final int REQUEST_OPEN_NEWS_BY_ID = 60005;

    public static final int GAME_REQUEST_C4_FILL_COLUMN = 1;
    public static final int GAME_REQUEST_C4_SHOW_WINNER = 2;
    public static final int GAME_REQUEST_C4_CHANGE_TURN = 3;

    public static final int GAME_REQUEST_SYNC_SESSION = 3;
    public static final int GAME_REQUEST_SYNC_GAME = 4;

    public static final int GAME_REQUEST_DB_FILL_LINE = 1;
    public static final int GAME_REQUEST_DB_FILL_BLOCKS = 2;
    public static final int GAME_REQUEST_DB_SET_WINNER = 3;
    public static final int GAME_REQUEST_DB_CHANGE_TURN = 5;

    public static final int GAME_REQUEST_TF_TAP_NUMBER = 1;
    public static final int GAME_REQUEST_TF_UPDATE_PROGRESS = 2;
    public static final int GAME_REQUEST_TF_RESET_PROGRESS = 3;

    public static final int GAME_REQUEST_MCH_SHOW_WINNER = 1;
    public static final int GAME_REQUEST_MCH_CHANGE_TURN = 2;
    public static final int GAME_REQUEST_MCH_ROLL_DICE = 3;
    public static final int GAME_REQUEST_MCH_MOVE_PAWN = 4;
    public static final int GAME_REQUEST_MCH_MOVE_PAWN_FINISHED = 5;

    public static final int GAME_REQUEST_BB_TAP_OPTION = 1;
    public static final int GAME_REQUEST_BB_UPDATE_PROGRESS = 2;
    public static final int GAME_REQUEST_BB_SET_QUESTION = 3;

    public static final int GAME_REQUEST_QW_SET_QUESTION = 1;
    public static final int GAME_REQUEST_QW_SYNC_CHOICES = 2;
    public static final int GAME_REQUEST_QW_BROADCAST_RESULT = 3;
    public static final int GAME_REQUEST_QW_SET_CHOICE = 4;
    public static final int GAME_REQUEST_QW_SHOW_OPTIONS = 5;

    public static final int GAME_REQUEST_FF_SET_PLAYER_POSITION = 1;
    public static final int GAME_REQUEST_FF_COLLECTABLE_COLLECTED = 2;
    public static final int GAME_REQUEST_FF_BOMB_COLLECTED = 3;
    public static final int GAME_REQUEST_FF_UPDATE_POSITIONS = 4;
    public static final int GAME_REQUEST_FF_PLACE_BOMB = 5;
    public static final int GAME_REQUEST_FF_ADD_TO_INVENTORY = 6;
    public static final int GAME_REQUEST_FF_REMOVE_FROM_INVENTORY = 7;
    public static final int GAME_REQUEST_FF_CHANGE_SCORE = 8;
    public static final int GAME_REQUEST_FF_BOMB_EXPLODE = 9;
    public static final int GAME_REQUEST_FF_SAW_COLLECTED = 10;
    public static final int GAME_REQUEST_FF_STOP_SAW = 11;
    public static final int GAME_REQUEST_FF_ACTIVATE_SAW = 12;
    public static final int GAME_REQUEST_FF_KILL_PLAYER = 13;

    public static final int GAME_REQUEST_WE_TAP_CHAR = 1;
    public static final int GAME_REQUEST_WE_SET_WORD = 2;
    public static final int GAME_REQUEST_WE_UPDATE_PROGRESS = 3;
    public static final int GAME_REQUEST_WE_RESET_WORD_PROGRESS = 4;


    public static final int GAME_REQUEST_GB_SEND_MESSAGE = 100;
    public static final int GAME_REQUEST_GB_GAME_READY_MESSAGE = 101;
    public static final int GAME_REQUEST_GB_ACKNOWLEDGE = 102;
    public static final int GAME_REQUEST_GB_SHAHRAM = 103;
    public static final int GAME_REQUEST_GB_LOBBY_READY_MESSAGE = 104;
    public static final int GAME_REQUEST_GB_ADD_PLAYER = 105;
    public static final int GAME_REQUEST_GB_REMOVE_PLAYER = 106;
    public static final int GAME_REQUEST_GB_HIGHLIGHT_PLAYER = 107;
    public static final int GAME_REQUEST_GB_SET_TIMER = 108;
    public static final int GAME_REQUEST_GB_CANCEL_TIMER = 109;
    public static final int GAME_REQUEST_GB_SEND_INGAME_MESSAGE = 110;
    public static final int GAME_REQUEST_GB_CHANGE_TURN = 111;

    public static final int GAME_REQUEST_CR_SET_LAST_MESSAGES = 200;
    public static final int GAME_REQUEST_CR_SET_CONTACT_LIST = 201;
    public static final int GAME_REQUEST_CR_SHOW_PRIVATE_MESSAGE = 202;

    public static final int GAME_REQUEST_SEND_FF_PLAYER_POSITION = 1046;
}
