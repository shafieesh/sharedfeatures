package com.chainedminds;

import com.chainedminds.api._FileHandler;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.api.account._Account;
import com.chainedminds.api.account._AccountSession;
import com.chainedminds.api.account._BlackList;
import com.chainedminds.api.account._Profile;
import com.chainedminds.api.store.*;
import com.chainedminds.models._ProductData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.models.payment._IPGTransactionData;
import com.chainedminds.utilities._File;
import com.chainedminds.utilities.database._DBConnection;
import com.chainedminds.utilities.database._Database;

public class _R<
        DBConnection extends _DBConnection,
        Database extends _Database,
        RequestHandler extends _RequestHandler<?>,
        FileHandler extends _FileHandler<?>,
        Profile extends _Profile,
        Account extends _Account,
        AccountSession extends _AccountSession,
        File extends _File,
        IABPayment extends _IABPayment<? extends _IABTransactionData, ? extends _ProductData>,
        IPGPayment extends _IPGPayment<? extends _IPGTransactionData, ? extends _ProductData>,
        IABProductPurchase extends _IABProductPurchase<? extends _IABTransactionData>,
        IPGProductPurchase extends _IPGProductPurchase<? extends _IPGTransactionData>,
        IABSubscriptionPurchase extends _IABSubscriptionPurchase<? extends _IABTransactionData>,
        IPGSubscriptionPurchase extends _IPGSubscriptionPurchase<? extends _IPGTransactionData>,
        Product extends _Product<? extends _ProductData>,
        BlackList extends _BlackList> {

    private static final _R<
            ? extends _DBConnection,
            ? extends _Database,
            ? extends _RequestHandler<?>,
            ? extends _FileHandler<?>,
            ? extends _Profile,
            ? extends _Account,
            ? extends _AccountSession,
            ? extends _File,
            ? extends _IABPayment<?, ?>,
            ? extends _IPGPayment<?, ?>,
            ? extends _IABProductPurchase<?>,
            ? extends _IPGProductPurchase<?>,
            ? extends _IABSubscriptionPurchase<?>,
            ? extends _IPGSubscriptionPurchase<?>,
            ? extends _Product<?>,
            ? extends _BlackList
            > INSTANCE = new _R<>();

    public static <
            DBConnection extends _DBConnection,
            Database extends _Database,
            RequestHandler extends _RequestHandler<?>,
            FileHandler extends _FileHandler<?>,
            Profile extends _Profile,
            Account extends _Account,
            AccountSession extends _AccountSession,
            File extends _File,
            IABPayment extends _IABPayment<? extends _IABTransactionData, ? extends _ProductData>,
            IPGPayment extends _IPGPayment<? extends _IPGTransactionData, ? extends _ProductData>,
            IABProductPurchase extends _IABProductPurchase<? extends _IABTransactionData>,
            IPGProductPurchase extends _IPGProductPurchase<? extends _IPGTransactionData>,
            IABSubscriptionPurchase extends _IABSubscriptionPurchase<? extends _IABTransactionData>,
            IPGSubscriptionPurchase extends _IPGSubscriptionPurchase<? extends _IPGTransactionData>,
            Product extends _Product<? extends _ProductData>,
            BlackList extends _BlackList> _R<
            DBConnection, Database, RequestHandler, FileHandler, Profile, Account, AccountSession,
            File, IABPayment, IPGPayment, IABProductPurchase, IPGProductPurchase,
            IABSubscriptionPurchase, IPGSubscriptionPurchase, Product, BlackList
            > get() {

        return (_R<
                DBConnection, Database, RequestHandler, FileHandler, Profile, Account, AccountSession,
                File, IABPayment, IPGPayment, IABProductPurchase,
                IPGProductPurchase, IABSubscriptionPurchase, IPGSubscriptionPurchase, Product, BlackList
                >) INSTANCE;
    }

    public DBConnection dbConnection;
    public Database database;
    public RequestHandler requestHandler;
    public FileHandler fileHandler;
    public Profile profile;
    public Account account;
    public AccountSession accountSession;
    public File file;
    public IABPayment iabPayment;
    public IPGPayment ipgPayment;
    public IABProductPurchase iabProductPurchase;
    public IPGProductPurchase ipgProductPurchase;
    public IABSubscriptionPurchase iabSubscriptionPurchase;
    public IPGSubscriptionPurchase ipgSubscriptionPurchase;
    public Product product;
    public BlackList blackList;
}