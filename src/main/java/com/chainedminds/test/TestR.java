package com.chainedminds.test;

import com.chainedminds._Classes;
import com.chainedminds._R;
import com.chainedminds.api.account._Account;
import com.chainedminds.api.account._AccountSession;
import com.chainedminds.api.account._BlackList;
import com.chainedminds.api.account._Profile;
import com.chainedminds.api.store.*;
import com.chainedminds.models._FileData;
import com.chainedminds.models._ProductData;
import com.chainedminds.models._ProfileData;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.models.notification._MessageData;
import com.chainedminds.models.notification._NotificationData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.models.payment._IPGTransactionData;
import com.chainedminds.utilities._File;
import com.chainedminds.utilities.database._DBConnection;
import com.chainedminds.utilities.database._Database;

import java.sql.Connection;

public class TestR extends _R<
        _DBConnection,
        _Database,
        TestRequestHandler,
        TestFileHandler,
        _Profile,
        _Account,
        _AccountSession,
        _File,
        _IABPayment<_IABTransactionData, _ProductData>,
        _IPGPayment<_IPGTransactionData, _ProductData>,
        _IABProductPurchase<_IABTransactionData>,
        _IPGProductPurchase<_IPGTransactionData>,
        _IABSubscriptionPurchase<_IABTransactionData>,
        _IPGSubscriptionPurchase<_IPGTransactionData>,
        _Product<_ProductData>,
        _BlackList> {

    public static _R<
            _DBConnection,
            _Database,
            TestRequestHandler,
            TestFileHandler,
            _Profile,
            _Account,
            _AccountSession,
            _File,
            _IABPayment<_IABTransactionData, _ProductData>,
            _IPGPayment<_IPGTransactionData, _ProductData>,
            _IABProductPurchase<_IABTransactionData>,
            _IPGProductPurchase<_IPGTransactionData>,
            _IABSubscriptionPurchase<_IABTransactionData>,
            _IPGSubscriptionPurchase<_IPGTransactionData>,
            _Product<_ProductData>,
            _BlackList> get() {

        return _R.get();
    }

    public static void config() {

        _R<_DBConnection, _Database, TestRequestHandler, TestFileHandler,
                _Profile, _Account, _AccountSession,
                _File, _IABPayment<_IABTransactionData, _ProductData>,
                _IPGPayment<_IPGTransactionData, _ProductData>,
                _IABProductPurchase<_IABTransactionData>,
                _IPGProductPurchase<_IPGTransactionData>,
                _IABSubscriptionPurchase<_IABTransactionData>,
                _IPGSubscriptionPurchase<_IPGTransactionData>,
                _Product<_ProductData>,
                _BlackList> resources = _R.get();

        resources.dbConnection = new _DBConnection() {
            @Override
            public String getPassword() {
                return super.getPassword();
            }

            @Override
            public String getUsername() {
                return super.getUsername();
            }

            @Override
            public String getAddress() {
                return super.getAddress();
            }
        };
        resources.database = new _Database() {
            @Override
            public Connection connect() {
                return super.connect();
            }

            @Override
            public void close(Connection connection) {
                super.close(connection);
            }
        };
        resources.requestHandler = new TestRequestHandler(TestData.class);
        resources.fileHandler = new TestFileHandler(TestData.class);
        resources.profile = new _Profile();
        resources.account = new _Account();
        resources.accountSession = new _AccountSession();
        resources.file = new _File();
        resources.iabPayment = new _IABPayment<>();
        resources.ipgPayment = new _IPGPayment<>();
        resources.iabProductPurchase = new _IABProductPurchase<>();
        resources.ipgProductPurchase = new _IPGProductPurchase<>();
        resources.iabSubscriptionPurchase = new _IABSubscriptionPurchase<>();
        resources.ipgSubscriptionPurchase = new _IPGSubscriptionPurchase<>();
        resources.product = new _Product<>();
        resources.blackList = new _BlackList();

        _Classes classes = _Classes.getInstance();
        classes.dataClass = TestData.class;
        classes.accountClass = _AccountData.class;
        classes.fileClass = _FileData.class;
        classes.iabTransactionClass = _IABTransactionData.class;
        classes.ipgTransactionClass = _IPGTransactionData.class;
        classes.productClass = _ProductData.class;
        classes.messageClass = _MessageData.class;
        classes.notificationClass = _NotificationData.class;
        classes.profileClass = _ProfileData.class;
    }
}