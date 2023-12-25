package com.chainedminds;

import com.chainedminds.models.*;
import com.chainedminds.models.account.BaseAccountData;
import com.chainedminds.models.account.BaseFriendData;
import com.chainedminds.models.notification.BaseNotificationData;
import com.chainedminds.models.payment.BaseIABTransactionData;
import com.chainedminds.models.payment.BaseIPGTransactionData;
import com.chainedminds.utilities.BaseLogs;

public class BaseClasses {

    private static final String TAG = BaseClasses.class.getSimpleName();

    private static final BaseClasses INSTANCE = new BaseClasses();

    public static BaseClasses getInstance() {

        return INSTANCE;
    }

    public Class<? extends BaseData<?>> dataClass;
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

            BaseLogs.severe(TAG, e);
        }

        throw new NullPointerException("Cannot construct from class " + mappedClass);
    }
}