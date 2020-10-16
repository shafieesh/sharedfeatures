package com.chainedminds.utilities;

import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.BaseResources;
import com.chainedminds.dataClasses.BaseMessageClass;
import com.chainedminds.dataClasses.firebase.FirebaseMessageData;
import com.chainedminds.dataClasses.notification.Action;
import com.chainedminds.dataClasses.notification.BaseNotificationData;
import com.chainedminds.dataClasses.notification.FirebaseResponseData;
import com.chainedminds.network.netty.NettyServer;
import com.chainedminds.utilities.json.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class BaseNotificationManager {

    public static final String TAG_FRIENDSHIP_NEW_FRIEND_REQUEST = "friendship_new_friendship_request";
    public static final String TAG_FRIENDSHIP_FRIEND_BECAME_ONLINE = "friendship_friend_became_online";
    public static final String TAG_FRIENDSHIP_ACCEPTED_YOUR_FRIENDSHIP = "friendship_accepted_your_friendship";

    private static final String TAG = BaseNotificationManager.class.getSimpleName();

    public static boolean timeIsNotDisturbing() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        return hour < 23 && hour >= 7;
    }

    /*public static void sendTalkingRoomsNewMessageNotification(int sessionID, long userID) {

        NettyServer.execute(() -> {

            try {

                Connection connection = ConnectionManager.openSharedUnsafe();

                ResultSet results = connection.createStatement().executeQuery("SELECT " +
                        FIELD_FIREBASE_ID_OLD + ", AppVersion, " + FIELD_PLATFORM_OLD + " FROM " + TABLE_ACCOUNTS_USERS +
                        " WHERE " + FIELD_USER_ID + " = " + userID);

                if (results.next()) {

                    String platform = results.getString(FIELD_PLATFORM_OLD);
                    int appVersion = results.getInt("AppVersion");

                    FirebaseClass content = new FirebaseClass();

                    content.priority = "high";
                    //content.registration_ids.add(results.getString(FIELD_FIREBASE_ID));
                    content.to = results.getString(FIELD_FIREBASE_ID_OLD);

                    /*content.notification = new HashMap<>();
                    content.notification.put("sound", "default");
                    content.notification.put("title", "Talking Rooms");
                    content.notification.put("body", "New message received");
                    content.notification.put("tag", "TalkingRooms");
                    content.notification.put("click_action", "http://tele-teb.com/talkingRooms/show/" + sessionID);*-/

                    content.data = new HashMap<>();
                    //content.data.put("section", "talking_rooms");
                    //content.data.put("tab", "new_questions");
                    //content.data.put("sessionID", sessionID + "");
                    //content.data.put("click_action", "http://tele-teb.com/talkingRooms/show/" + sessionID);
                    //content.data.put("action", "OPEN_URI");
                    content.data.put("action", "OPEN_WEBSITE");
                    //content.data.put("action", "SHOW_WEBSITE");
                    //content.data.put("url", "http://www.tele-teb.com/talkingRooms/show/" + sessionID);
                    //content.data.put("url", "teleteb_beauty://talkingRooms/show/" + sessionID);
                    //content.data.put("uri", "teleteb_beauty://open?section=consultation");
                    content.data.put("action", "NOTIFY_WITH_ACTION");
                    content.data.put("notification_title", "Title");
                    content.data.put("notification_message", "Message...");

                    content.data.put("uri", "teleteb_consultation://open?action=navigate,showMessage&showMessage&section=talking_rooms&tab=my_questions");
                    content.data.put("notification_uri", "teleteb_pregnancy://open?action=showMessage,navigate&title=Takhfife Vije!&message=adhisg fiuyeg fiue fiuaeg f.&section=talking_rooms&tab=my_questions&activity=ShowTalkingRoom&payload=111");
                    content.data.put("notification_uri", "teleteb_pregnancy://open?action=navigate&title=Takhfife Vije!&message=adhisg fiuyeg fiue fiuaeg f.&section=talking_rooms&tab=my_questions&activity=ShowTalkingRoom&payload=17605");
                    //content.data.put("notification_uri", "http://tele-teb.com");
                    //content.data.put("url", "http://store.tele-teb.com");
                    //content.data.put("uri", "tg://resolve?domain=tele_teb");
                    //content.data.put("uri", "tel:+982142638932");

                    if (PLATFORM_ANDROID.equals(platform)) {

                        if (appVersion >= 13) {

                            sendToFirebase(FIREBASE_KEY, content);

                        } else {

                            sendToFirebase(FIREBASE_KEY_OLD, content);
                        }

                    } else {

                        sendToFirebase(FIREBASE_KEY, content);
                    }
                }

                results.close();
                ConnectionManager.close(connection);

            } catch (SQLException e) {

                e.printStackTrace();
            }
        });
    }

    public static void notifyAndOpenUri(long userID, String appName, String title, String message, String uri) {

        NettyServer.execute(() -> {

            String firebaseID = UsersAccountManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseClass content = new FirebaseClass();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "NOTIFY_WITH_ACTION");
                content.data.put("notification_title", title);
                content.data.put("notification_message", message);
                content.data.put("notification_uri", uri);
                content.data.put("uri", uri);

                sendToFirebase(FIREBASE_KEY, content);
            }
        });
    }

    public static void openUri(long userID, String appName, String uri) {

        NettyServer.execute(() -> {

            String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseClass content = new FirebaseClass();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "OPEN_URI");
                content.data.put("uri", uri);

                sendToFirebase(FIREBASE_KEY, content);
            }
        });
    }

    public static void notifyEveryoneAndOpenUri(String appName, String title, String message, String uri) {

        NettyServer.execute(() -> {

            Set<String> firebaseIDs = UsersAccountManager.getAllFirebaseIDs(appName);

            for (String firebaseID : firebaseIDs) {

                FirebaseClass content = new FirebaseClass();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "NOTIFY_WITH_ACTION");
                content.data.put("notification_title", title);
                content.data.put("notification_message", message);
                content.data.put("notification_uri", uri);
                content.data.put("uri", uri);

                sendToFirebase(FIREBASE_KEY, content);
            }
        });
    }

    public static void notifyWithDialog(long userID, String appName, String title, String message) {

        NettyServer.execute(() -> {

            String appScheme = BackendHelper.getAppScheme(appName);

            String firebaseID = UsersAccountManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseClass content = new FirebaseClass();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "NOTIFY_WITH_ACTION");
                content.data.put("notification_title", title);
                content.data.put("notification_message", message);
                content.data.put("notification_uri", appScheme +
                        "://open?action=showMessage&title=" + title + "&message=" + message);
                content.data.put("uri", appScheme +
                        "://open?action=showMessage&title=" + title + "&message=" + message);


                sendToFirebase(FIREBASE_KEY, content);
            }
        });
    }

    public static void notifyEveryoneWithDialog(String appName, String title, String message) {

        NettyServer.execute(() -> {

            String appScheme = BackendHelper.getAppScheme(appName);

            Set<String> firebaseIDs = UsersAccountManager.getAllFirebaseIDs(appName);

            for (String firebaseID : firebaseIDs) {

                FirebaseClass content = new FirebaseClass();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "NOTIFY_WITH_ACTION");
                content.data.put("notification_title", title);
                content.data.put("notification_message", message);
                content.data.put("notification_uri", appScheme +
                        "://open?action=showMessage&title=" + title + "&message=" + message);
                content.data.put("uri", appScheme +
                        "://open?action=showMessage&title=" + title + "&message=" + message);

                sendToFirebase(FIREBASE_KEY, content);
            }
        });
    }

    public static void notifyNewTalkingRoomMessage(String appName, int roomID, String message) {

        NettyServer.execute(() -> {

            RoomClass room = TalkingRooms.getBasicRoom(roomID);

            if (room == null) {

                return;
            }

            long userID = room.owner.id;
            String firebaseID = UsersAccountManager.getFirebaseID(userID, appName);
            String language = UsersAccountManager.getLanguage(userID);
            String title = SystemMessages.getMessage(BaseConfig.MESSAGE_TALKING_ROOM, language);
            String appScheme = BackendHelper.getAppScheme(appName);

            if (firebaseID != null) {

                FirebaseClass content = new FirebaseClass();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "NOTIFY_WITH_ACTION");
                content.data.put("notification_title", title);
                content.data.put("notification_message", message);

                content.data.put("notification_uri", appScheme + "://open?action=navigate" +
                        "&section=talking_rooms&tab=my_questions&activity=" +
                        "ShowTalkingRoom&payload=" + roomID);

                content.data.put("uri", appScheme + "://open?action=navigate" +
                        "&section=talking_rooms&tab=my_questions&activity=" +
                        "ShowTalkingRoom&payload=" + roomID);

                sendToFirebase(FIREBASE_KEY, content);
            }
        });
    }

    public static DataClass sendNotification(DataClass data) {

        data.responseCode = BaseCodes.RESPONSE_NOK;

        String language = data.client.language;

        if (data.notification == null || data.notification.appNames == null ||
                data.notification.title == null || data.notification.message == null) {

            data.responseCode = BaseCodes.RESPONSE_NOK;
            data.message = SystemMessages.getMessage(MESSAGE_MISSING_DATA, language);
            return data;
        }

        List<String> appNames = data.notification.appNames;
        String title = data.notification.title;
        String message = data.notification.message;
        long userID = data.notification.sendTo;

        for (String appName : appNames) {

            sendNotification(appName, title, message, userID);
        }

        data.responseCode = BaseCodes.RESPONSE_OK;

        return data;
    }

    public static void sendNotification(String title, String message, long userID) {

        for (String product : Mapping.get(Mapping.PRODUCTS, Language.EN)) {

            sendNotification(product, title, message, userID);
        }
    }*/

    public static void sendNotification(int userID, String appName, String title, String message) {

        sendNotification(userID, appName, null, title, message);
    }

    public static boolean notifyUser(int userID, String appName, String title, String content) {

        String firebaseID = BaseResources.getInstance().accountPropertyManager.getFirebaseID(userID, appName);

        if (firebaseID != null) {

            BaseNotificationData notification = new BaseNotificationData();

            notification.title = title;
            notification.content = content;

            notification.action = new Action();
            notification.action.request = BaseCodes.REQUEST_OPEN_PAGE;
            notification.action.subRequest = BaseCodes.REQUEST_OPEN_LOTTERY;

            FirebaseMessageData notificationContent = new FirebaseMessageData();
            notificationContent.priority = "high";
            notificationContent.to = firebaseID;

            notificationContent.data = new HashMap<>();
            notificationContent.data.put("type", "NOTIFICATION");
            notificationContent.data.put("notification", JsonHelper.getString(notification));

            return sendToFirebase(userID, notificationContent);
        }

        return false;
    }

    public static void sendNotification(int userID, String appName, String tag, String title, String message) {

        NettyServer.execute(() -> {

            String firebaseID = BaseResources.getInstance().accountPropertyManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseMessageData messageData = new FirebaseMessageData();
                messageData.priority = "high";
                messageData.to = firebaseID;

                messageData.data = new HashMap<>();
                messageData.data.put("action", "NOTIFY");
                messageData.data.put("notification_title", title);
                messageData.data.put("notification_message", message);
                messageData.data.put("notification_tag", tag);

                sendToFirebase(userID, messageData);
            }
        });
    }

    public static void sendTestNotification(int userID, String appName, String title, String message, String image) {

        NettyServer.execute(() -> {

            String firebaseID = BaseResources.getInstance().accountPropertyManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseMessageData messageData = new FirebaseMessageData();
                messageData.priority = "high";
                messageData.to = firebaseID;

                messageData.notification = new FirebaseMessageData.NotificationData();
                messageData.notification.title = title;
                messageData.notification.body = message;
                messageData.notification.image = image;

                sendToFirebase(userID, messageData);
            }
        });
    }

    public static void sendNotificationWithID(int userID, String firebaseID, String tag, String title, String message) {

        if (firebaseID != null) {

            FirebaseMessageData content = new FirebaseMessageData();
            content.priority = "high";
            content.to = firebaseID;

            content.data = new HashMap<>();
            content.data.put("action", "NOTIFY");
            content.data.put("notification_title", title);
            content.data.put("notification_message", message);
            content.data.put("notification_tag", tag);

            sendToFirebase(userID, content);
        }
    }

    public static void sendNotification(String gamerTag, String appName, String title, String message) {

        NettyServer.execute(() -> {

            int userID = BaseResources.getInstance().accountManager.findUserID(gamerTag);

            String firebaseID = BaseResources.getInstance().accountPropertyManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseMessageData content = new FirebaseMessageData();
                content.priority = "high";
                content.to = firebaseID;

                content.data = new HashMap<>();
                content.data.put("action", "NOTIFY");
                content.data.put("notification_title", title);
                content.data.put("notification_message", message);

                sendToFirebase(userID, content);
            }
        });
    }

    public static void sendMessage(String gamerTag, String appName, String message) {

        NettyServer.execute(() -> {

            int userID = BaseResources.getInstance().accountManager.findUserID(gamerTag);

            String firebaseID = BaseResources.getInstance().accountPropertyManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseMessageData content = new FirebaseMessageData();
                content.priority = "high";
                content.to = firebaseID;

                Action action = new Action();
                action.request = BaseCodes.REQUEST_BROADCAST_MESSAGE;
                action.message = new BaseMessageClass();
                action.message.message = message;

                content.data = new HashMap<>();
                content.data.put("type", "ACTION");
                content.data.put("action", JsonHelper.getString(action));

                sendToFirebase(userID, content);
            }
        });
    }

    public static void sendMessage(int userID, String appName, String message) {

        NettyServer.execute(() -> {

            String firebaseID = BaseResources.getInstance().accountPropertyManager.getFirebaseID(userID, appName);

            if (firebaseID != null) {

                FirebaseMessageData content = new FirebaseMessageData();
                content.priority = "high";
                content.to = firebaseID;

                Action action = new Action();
                action.request = BaseCodes.REQUEST_BROADCAST_MESSAGE;
                action.message = new BaseMessageClass();
                action.message.message = message;

                content.data = new HashMap<>();
                content.data.put("type", "ACTION");
                content.data.put("action", JsonHelper.getString(action));

                sendToFirebase(userID, content);
            }
        });
    }

//    public static void sendNotification(String topic, String appName, String title, String message) {
//
//        NettyServer.execute(() -> {
//
//            String platform = "Android"; //UsersAccountManager.getPlatform(userID, appName);
//            int appVersion = 1; //UsersAccountManager.getAppVersion(userID, appName);
//
//            FirebaseClass content = new FirebaseClass();
//            content.priority = "high";
//            content.to = topic;
//
//            /*if (BaseConfig.PLATFORM_ANDROID.equals(platform) && appVersion > 30) {
//
//                content.data = new HashMap<>();
//                content.data.put("action", "NOTIFY");
//                content.data.put("notification_title", title);
//                content.data.put("notification_message", message);
//                content.data.put("notification_tag", "Consultation");
//
//            } else {*-/
//
//            content.notification = new HashMap<>();
//            content.notification.put("title", title);
//            content.notification.put("body", message);
//            content.notification.put("sound", "default");
//            content.notification.put("android_channel_id", "default_notifications");
//            //}
//
//            sendToFirebase(content);
//        });
//    }

    protected static boolean sendToFirebase(int userID, FirebaseMessageData content) {

        boolean wasSuccessful = false;

        try {

            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("213.32.14.69", 65000));

            String json = JsonHelper.getString(content);

            URL url = new URL("https://fcm.googleapis.com/fcm/send");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setDoOutput(true);

            connection.setRequestProperty("project_id", BaseConfig.FIREBASE_PROJECT_ID);
            connection.setRequestProperty("Authorization", "key=" + BaseConfig.FIREBASE_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.connect();

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            InputStream inputStream;

            if (connection.getResponseCode() == 200) {

                inputStream = connection.getInputStream();

                wasSuccessful = true;

            } else {

                inputStream = connection.getErrorStream();
            }

            String responseString = new Scanner(inputStream, "UTF_8").useDelimiter("\\A").next();

            inputStream.close();

            connection.disconnect();

            FirebaseResponseData responseData = JsonHelper.getObject(responseString, FirebaseResponseData.class);

            if (responseData != null) {

                if (responseData.results != null && responseData.results.size() > 0) {

                    String error = responseData.results.get(0).error;

                    if ("NotRegistered".equals(error) || "InvalidRegistration".equals(error)) {

                        BaseResources.getInstance().accountPropertyManager.removeFirebaseID(userID, content.to);
                    }
                }
            }

            System.out.println(responseString);

        } catch (IOException e) {

            Log.error(TAG, e);
        }

        return wasSuccessful;
    }

    public static void reportBruteForce(String address, String username, String password) {

        String notificationTitle = "Brute Force Attempt";
        String notificationMessage = "IP Address : " + address + "\n" +
                "Username : " + username + "\n" +
                "Password : " + password;

        for (String adminGamerTag : BaseConfig.ADMINS) {

            sendNotification(adminGamerTag, BaseConfig.APP_NAME_CAFE_GAME,
                    notificationTitle, notificationMessage);
        }
    }

    public static void reportLogin(String username, String password, Boolean status, String address) {

        String loginStatus = status != null ? status + "" : "false";

        String notificationTitle = "Login Attempt";
        String notificationMessage = "Username : " + username + "\n" +
                "Password : " + password + "\n" +
                "Authenticated : " + loginStatus + "\n" +
                "IP Address : " + address;

        for (String adminGamerTag : BaseConfig.ADMINS) {

            sendNotification(adminGamerTag, BaseConfig.APP_NAME_CAFE_GAME,
                    notificationTitle, notificationMessage);
        }
    }

    public static void reportError(String appName, String error) {

        //int userID = UsersAccountManager.getUserID("ShayanSh2");

        //String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, "CafeGame");

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "تیکت قرعه کشی بخر!";
        notification.content = "کمتر از چند ساعت دیگه مونده تا نتایج قرعه کشی مشخص بشه!";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();
        notification.action.request = BaseCodes.REQUEST_OPEN_PAGE;
        notification.action.subRequest = BaseCodes.REQUEST_OPEN_LOTTERY;

        /*notification.buttons = new ArrayList<>();

        NotificationButton button1 = new NotificationButton();
        button1.title = "قبول کردن";
        button1.action = new Action();
        button1.action.request = 1;
        button1.action.account = new Account();
        button1.action.account.id = 10;

        NotificationButton button2 = new NotificationButton();
        button2.title = "رد کردن";
        button2.action = new Action();
        button2.action.request = 55;
        button1.action.account = new Account();
        button1.action.account.id = 222211233;

        button1.action.message = new BaseMessageClass();
        button1.action.message.message = "خبر جدید رسید!";

        notification.buttons.add(button1);
        notification.buttons.add(button2);*/

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/Admin";

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));
        //content.data.put("action", JsonHelper.getString(action));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }

    public static void a(String topic) {

        //int userID = UsersAccountManager.getUserID("ShayanSh2");

        //String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, "CafeGame");

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "تیکت قرعه کشی بخر!";
        notification.content = "کمتر از چند ساعت دیگه مونده تا نتایج قرعه کشی مشخص بشه!";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();
        notification.action.request = BaseCodes.REQUEST_OPEN_PAGE;
        notification.action.subRequest = BaseCodes.REQUEST_OPEN_LOTTERY;

        /*notification.buttons = new ArrayList<>();

        NotificationButton button1 = new NotificationButton();
        button1.title = "قبول کردن";
        button1.action = new Action();
        button1.action.request = 1;
        button1.action.account = new Account();
        button1.action.account.id = 10;

        NotificationButton button2 = new NotificationButton();
        button2.title = "رد کردن";
        button2.action = new Action();
        button2.action.request = 55;
        button1.action.account = new Account();
        button1.action.account.id = 222211233;

        button1.action.message = new BaseMessageClass();
        button1.action.message.message = "خبر جدید رسید!";

        notification.buttons.add(button1);
        notification.buttons.add(button2);*/

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/" + topic;

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));
        //content.data.put("action", JsonHelper.getString(action));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }

    public static void b(String topic) {

        //int userID = UsersAccountManager.getUserID("ShayanSh2");

        //String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, "CafeGame");

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "!جایزه! جایزه";
        notification.content = "گردونه قرعه کشی بالاخره وایساد. بیا ببین کیا برنده شدن!";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();
        notification.action.request = BaseCodes.REQUEST_OPEN_PAGE;
        //notification.action.subRequest = BaseCodes.REQUEST_OPEN_LOTTERY;

        /*notification.buttons = new ArrayList<>();

        NotificationButton button1 = new NotificationButton();
        button1.title = "قبول کردن";
        button1.action = new Action();
        button1.action.request = 1;
        button1.action.account = new Account();
        button1.action.account.id = 10;

        NotificationButton button2 = new NotificationButton();
        button2.title = "رد کردن";
        button2.action = new Action();
        button2.action.request = 55;
        button1.action.account = new Account();
        button1.action.account.id = 222211233;

        button1.action.message = new BaseMessageClass();
        button1.action.message.message = "خبر جدید رسید!";

        notification.buttons.add(button1);
        notification.buttons.add(button2);*/

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/" + topic;

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));
        //content.data.put("action", JsonHelper.getString(action));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }

    public static void b2(String topic) {

        //int userID = UsersAccountManager.getUserID("ShayanSh2");

        //String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, "CafeGame");

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "Prizes! Prizes!";
        notification.content = "The lottery is now over! Come and see who the winners are!";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();
        notification.action.request = BaseCodes.REQUEST_OPEN_PAGE;
        //notification.action.subRequest = BaseCodes.REQUEST_OPEN_LOTTERY;

        /*notification.buttons = new ArrayList<>();

        NotificationButton button1 = new NotificationButton();
        button1.title = "قبول کردن";
        button1.action = new Action();
        button1.action.request = 1;
        button1.action.account = new Account();
        button1.action.account.id = 10;

        NotificationButton button2 = new NotificationButton();
        button2.title = "رد کردن";
        button2.action = new Action();
        button2.action.request = 55;
        button1.action.account = new Account();
        button1.action.account.id = 222211233;

        button1.action.message = new BaseMessageClass();
        button1.action.message.message = "خبر جدید رسید!";

        notification.buttons.add(button1);
        notification.buttons.add(button2);*/

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/" + topic;

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));
        //content.data.put("action", JsonHelper.getString(action));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }

    public static void c(String topic) {

        //int userID = UsersAccountManager.getUserID("ShayanSh2");

        //String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, "CafeGame");

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "اختلال سراسری در کافه";
        notification.content = "متاسفانه اختلالاتی در شبکه به وجود آمده که در حال پیگیری برای حل مشکل هستیم.";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();

        /*notification.buttons = new ArrayList<>();

        NotificationButton button1 = new NotificationButton();
        button1.title = "قبول کردن";
        button1.action = new Action();
        button1.action.request = 1;
        button1.action.account = new Account();
        button1.action.account.id = 10;

        NotificationButton button2 = new NotificationButton();
        button2.title = "رد کردن";
        button2.action = new Action();
        button2.action.request = 55;
        button1.action.account = new Account();
        button1.action.account.id = 222211233;

        button1.action.message = new BaseMessageClass();
        button1.action.message.message = "خبر جدید رسید!";

        notification.buttons.add(button1);
        notification.buttons.add(button2);*/

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/" + topic;

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));
        //content.data.put("action", JsonHelper.getString(action));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }

    public static void d(String topic) {

        //int userID = UsersAccountManager.getUserID("ShayanSh2");

        //String firebaseID = UsersAccountPropertiesManager.getFirebaseID(userID, "CafeGame");

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "برطرف شدن اختلال سراسری";
        notification.content = "خوشبختانه هم اکنون مشکل سراسری حل شده و کافه گیم بار دیگر در کنار شماست.";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();

        /*notification.buttons = new ArrayList<>();

        NotificationButton button1 = new NotificationButton();
        button1.title = "قبول کردن";
        button1.action = new Action();
        button1.action.request = 1;
        button1.action.account = new Account();
        button1.action.account.id = 10;

        NotificationButton button2 = new NotificationButton();
        button2.title = "رد کردن";
        button2.action = new Action();
        button2.action.request = 55;
        button1.action.account = new Account();
        button1.action.account.id = 222211233;

        button1.action.message = new BaseMessageClass();
        button1.action.message.message = "خبر جدید رسید!";

        notification.buttons.add(button1);
        notification.buttons.add(button2);*/

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/" + topic;

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));
        //content.data.put("action", JsonHelper.getString(action));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }

    public static void z(String topic) {

        BaseNotificationData notification = new BaseNotificationData();

        notification.title = "یلدایتان مبارک! \uD83C\uDF49\uD83C\uDF49";
        notification.content = "تا ۷۰ درصد تخفیف در فروشگاه کافه گیم به مناسبت شب یلدا!";
        //notification.tag = "friendship_new_friendship_request";

        notification.action = new Action();
        notification.action.request = BaseCodes.REQUEST_OPEN_PAGE;
        notification.action.subRequest = BaseCodes.REQUEST_OPEN_PAGE;

        FirebaseMessageData content = new FirebaseMessageData();
        content.priority = "high";
        content.to = "/topics/" + topic;

        content.data = new HashMap<>();
        content.data.put("type", "NOTIFICATION");
        content.data.put("notification", JsonHelper.getString(notification));

        sendToFirebase(BaseCodes.NOT_FOUND, content);
    }
}