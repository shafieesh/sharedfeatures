package com.chainedminds;

import com.chainedminds.models.*;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.models.account._FriendData;
import com.chainedminds.models.notification._MessageData;
import com.chainedminds.models.notification._NotificationData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.models.payment._IPGTransactionData;
import com.chainedminds.utilities._Logs;

public class _Classes {

    private static final String TAG = _Classes.class.getSimpleName();

    private static final _Classes INSTANCE = new _Classes();

    public static _Classes getInstance() {

        return INSTANCE;
    }

    public Class<? extends _Data<?>> dataClass;
    public Class<? extends _AccountData> accountClass;
    public Class<? extends _FriendData> friendClass;
    public Class<? extends _FileData> fileClass;
    public Class<? extends _IABTransactionData> iabTransactionClass;
    public Class<? extends _IPGTransactionData> ipgTransactionClass;
    public Class<? extends _ProductData> productClass;
    public Class<? extends _MessageData> messageClass;
    public Class<? extends _NotificationData> notificationClass;
    public Class<? extends _ProfileData> profileClass;

    public static <T> T construct(Class<T> mappedClass) throws NullPointerException {

        try {

            return mappedClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {

            e.printStackTrace();

            _Logs.severe(TAG, e);
        }

        throw new NullPointerException("Cannot construct from class " + mappedClass);
    }
}