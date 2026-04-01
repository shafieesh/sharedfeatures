package com.chainedminds.test;

import com.chainedminds._Classes;
import com.chainedminds._Resources;
import com.chainedminds.api.account._Accounts;
import com.chainedminds.api.account._AccountSessions;
import com.chainedminds.api.account._BlackList;
import com.chainedminds.api.account._Profile;
import com.chainedminds.api.friendship._Friendship;
import com.chainedminds.api.store.*;
import com.chainedminds.models._FileData;
import com.chainedminds.models._ProductData;
import com.chainedminds.models._ProfileData;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.models.account._FriendData;
import com.chainedminds.models.notification._MessageData;
import com.chainedminds.models.notification._NotificationData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.models.payment._IPGTransactionData;
import com.chainedminds.utilities._File;
import com.chainedminds.utilities._Log;

public class TestResources extends _Resources<
        TestRequestHandler,
        _Profile,
        _Accounts,
        _AccountSessions,
        _Friendship,
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

    public static _Resources<
            TestRequestHandler,
            _Profile,
            _Accounts,
            _AccountSessions,
            _Friendship,
            _File,
            _IABPayment<_IABTransactionData, _ProductData>,
            _IPGPayment<_IPGTransactionData, _ProductData>,
            _IABProductPurchase<_IABTransactionData>,
            _IPGProductPurchase<_IPGTransactionData>,
            _IABSubscriptionPurchase<_IABTransactionData>,
            _IPGSubscriptionPurchase<_IPGTransactionData>,
            _Product<_ProductData>,
            _BlackList,
            _Log> get() {

        return _Resources.get();
    }

    public static void config() {

        _Resources<TestRequestHandler, _Profile, _Accounts, _AccountSessions,
                _Friendship,
                _File, _IABPayment<_IABTransactionData, _ProductData>,
                _IPGPayment<_IPGTransactionData, _ProductData>,
                _IABProductPurchase<_IABTransactionData>,
                _IPGProductPurchase<_IPGTransactionData>,
                _IABSubscriptionPurchase<_IABTransactionData>,
                _IPGSubscriptionPurchase<_IPGTransactionData>,
                _Product<_ProductData>,
                _BlackList, _Log> resources = _Resources.get();

        resources.requestHandler = new TestRequestHandler(TestData.class);
        resources.profile = new _Profile();
        resources.account = new _Accounts();
        resources.accountSession = new _AccountSessions();
        resources.friendship = new _Friendship();
        resources.file = new _File();
        resources.iabPayment = new _IABPayment<>();
        resources.ipgPayment = new _IPGPayment<>();
        resources.iabProductPurchase = new _IABProductPurchase<>();
        resources.ipgProductPurchase = new _IPGProductPurchase<>();
        resources.iabSubscriptionPurchase = new _IABSubscriptionPurchase<>();
        resources.ipgSubscriptionPurchase = new _IPGSubscriptionPurchase<>();
        resources.product = new _Product<>();
        resources.blackList = new _BlackList();
        resources.log = new _Log();

        _Classes classes = _Classes.getInstance();

        classes.dataClass = TestData.class;
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