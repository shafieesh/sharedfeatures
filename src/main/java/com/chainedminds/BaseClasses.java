package com.chainedminds;

import com.chainedminds.dataClasses.*;
import com.chainedminds.dataClasses.account.BaseAccountData;
import com.chainedminds.dataClasses.account.BaseFriendData;
import com.chainedminds.dataClasses.notification.BaseNotificationData;
import com.chainedminds.dataClasses.payment.BaseIABTransactionData;
import com.chainedminds.dataClasses.payment.BaseIPGTransactionData;
import com.chainedminds.utilities.Log;

public class BaseClasses {

    private static final String TAG = BaseClasses.class.getSimpleName();

    private static final BaseClasses INSTANCE = new BaseClasses();

    public static BaseClasses getInstance() {

        return INSTANCE;
    }

    public Class dataClass;
    public Class<? extends BaseAccountData> accountClass;
    public Class<? extends BaseFriendData> friendClass;
    public Class<? extends BaseFileData> fileClass;
    public Class<? extends BaseIABTransactionData> iabTransactionClass;
    public Class<? extends BaseIPGTransactionData> ipgTransactionClass;
    public Class<? extends BaseProductData> productClass;
    public Class<? extends BaseMessageData> messageClass;
    public Class<? extends BaseNotificationData> notificationClass;
    public Class<? extends BaseNewsData> newsClass;
    public Class<? extends BaseProfileData> profileClass;

    public static <T> T construct(Class<T> mappedClass) throws NullPointerException {

        try {

            return mappedClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {

            e.printStackTrace();

            Log.severe(TAG, e);
        }

        throw new NullPointerException("Cannot construct from class " + mappedClass);
    }
}