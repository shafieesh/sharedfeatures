package com.chainedminds.test;

import com.chainedminds._Classes;
import com.chainedminds._Resources;
import com.chainedminds.api.accounting._Account;
import com.chainedminds.api.accounting._AccountSession;
import com.chainedminds.api.accounting._BlackList;
import com.chainedminds.api.accounting._Profile;
import com.chainedminds.api.friendship._Friendship;
import com.chainedminds.api.store.*;
import com.chainedminds.models.*;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.models.account._FriendData;
import com.chainedminds.models.notification._MessageData;
import com.chainedminds.models.notification._NotificationData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.models.payment._IPGTransactionData;
import com.chainedminds.utilities._File;
import com.chainedminds.utilities._Log;

public class Resources extends _Resources<
        RequestHandler,
        _Profile,
        _Account<Data>,
        _AccountSession,
        _Friendship<Data, _FriendData>,
        _File,
        _IABPayment<_IABTransactionData, _ProductData>,
        _IPGPayment<_IPGTransactionData, _ProductData>,
        _IABProductPurchase<_IABTransactionData>,
        _IPGProductPurchase<_IPGTransactionData>,
        _IABSubscriptionPurchase<_IABTransactionData>,
        _IPGSubscriptionPurchase<_IPGTransactionData>,
        _Product<_ProductData>,
        _BlackList,
        _Log> {

    private static final Resources INSTANCE = new Resources();

    public static Resources getInstance() {

        return INSTANCE;
    }

    public void config() {

        super.requestHandler = new RequestHandler(Data.class);
        super.profile = new _Profile();
        super.account = new _Account<>();
        super.accountSession = new _AccountSession();
        super.friendship = new _Friendship<>();
        super.file = new _File();
        super.iabPayment = new _IABPayment<>();
        super.ipgPayment = new _IPGPayment<>();
        super.iabProductPurchase = new _IABProductPurchase<>();
        super.ipgProductPurchase = new _IPGProductPurchase<>();
        super.iabSubscriptionPurchase = new _IABSubscriptionPurchase<>();
        super.ipgSubscriptionPurchase = new _IPGSubscriptionPurchase<>();
        super.product = new _Product<>();
        super.blackList = new _BlackList();
        super.log = new _Log();

        _Resources<RequestHandler, _Profile, _Account<Data>, _AccountSession,
                        _Friendship<Data, _FriendData>,
                _File, _IABPayment<_IABTransactionData, _ProductData>,
                        _IPGPayment<_IPGTransactionData, _ProductData>,
                        _IABProductPurchase<_IABTransactionData>,
                        _IPGProductPurchase<_IPGTransactionData>,
                        _IABSubscriptionPurchase<_IABTransactionData>,
                        _IPGSubscriptionPurchase<_IPGTransactionData>,
                        _Product<_ProductData>,
                        _BlackList, _Log> resources = _Resources.getInstance();

        resources.requestHandler = super.requestHandler;
        resources.profile = super.profile;
        resources.account = super.account;
        resources.accountSession = super.accountSession;
        resources.friendship = super.friendship;
        resources.file = super.file;
        resources.iabPayment = super.iabPayment;
        resources.ipgPayment = super.ipgPayment;
        resources.iabProductPurchase = super.iabProductPurchase;
        resources.ipgProductPurchase = super.ipgProductPurchase;
        resources.iabSubscriptionPurchase = super.iabSubscriptionPurchase;
        resources.ipgSubscriptionPurchase = super.ipgSubscriptionPurchase;
        resources.product = super.product;
        resources.blackList = super.blackList;
        resources.log = super.log;

        _Classes classes = _Classes.getInstance();

        classes.dataClass = Data.class;
        classes.accountClass = _AccountData.class;
        classes.friendClass = _FriendData.class;
        classes.fileClass = _FileData.class;
        classes.iabTransactionClass = _IABTransactionData.class;
        classes.ipgTransactionClass = _IPGTransactionData.class;
        classes.productClass = _ProductData.class;
        classes.messageClass = _MessageData.class;
        classes.notificationClass = _NotificationData.class;
        classes.profileClass = _ProfileData.class;
    }
}