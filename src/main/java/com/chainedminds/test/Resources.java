package com.chainedminds.test;

import com.chainedminds._Classes;
import com.chainedminds._Resources;
import com.chainedminds.api.accounting._AccountManager;
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

public class Resources extends _Resources<
        RequestHandler,
        _Profile,
        _AccountManager<Data>,
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
        _BlackList> {

    private static final Resources INSTANCE = new Resources();

    public static Resources getInstance() {

        return INSTANCE;
    }

    public void config() {

        super.requestManager = new RequestHandler(Data.class);
        super.profileManager = new _Profile();
        super.accountManager = new _AccountManager<>();
        super.accountPropertyManager = new _AccountSession();
        super.friendshipManager = new _Friendship<>();
        super.fileManager = new _File();
        super.iabPaymentManager = new _IABPayment<>();
        super.ipgPaymentManager = new _IPGPayment<>();
        super.iabProductPurchasesManager = new _IABProductPurchase<>();
        super.ipgProductPurchasesManager = new _IPGProductPurchase<>();
        super.iabSubscriptionPurchasesManager = new _IABSubscriptionPurchase<>();
        super.ipgSubscriptionPurchasesManager = new _IPGSubscriptionPurchase<>();
        super.productManager = new _Product<>();
        super.blackListManager = new _BlackList();

        _Resources<RequestHandler, _Profile, _AccountManager<Data>, _AccountSession,
                        _Friendship<Data, _FriendData>,
                _File, _IABPayment<_IABTransactionData, _ProductData>,
                        _IPGPayment<_IPGTransactionData, _ProductData>,
                        _IABProductPurchase<_IABTransactionData>,
                        _IPGProductPurchase<_IPGTransactionData>,
                        _IABSubscriptionPurchase<_IABTransactionData>,
                        _IPGSubscriptionPurchase<_IPGTransactionData>,
                        _Product<_ProductData>,
                        _BlackList> resources = _Resources.getInstance();

        resources.requestManager = super.requestManager;
        resources.profileManager = super.profileManager;
        resources.accountManager = super.accountManager;
        resources.accountPropertyManager = super.accountPropertyManager;
        resources.friendshipManager = super.friendshipManager;
        resources.fileManager = super.fileManager;
        resources.iabPaymentManager = super.iabPaymentManager;
        resources.ipgPaymentManager = super.ipgPaymentManager;
        resources.iabProductPurchasesManager = super.iabProductPurchasesManager;
        resources.ipgProductPurchasesManager = super.ipgProductPurchasesManager;
        resources.iabSubscriptionPurchasesManager = super.iabSubscriptionPurchasesManager;
        resources.ipgSubscriptionPurchasesManager = super.ipgSubscriptionPurchasesManager;
        resources.productManager = super.productManager;
        resources.blackListManager = super.blackListManager;

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