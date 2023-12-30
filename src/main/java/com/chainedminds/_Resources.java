package com.chainedminds;

import com.chainedminds.api._RequestHandler;
import com.chainedminds.api.accounting._Account;
import com.chainedminds.api.accounting._AccountSession;
import com.chainedminds.api.accounting._BlackList;
import com.chainedminds.api.accounting._Profile;
import com.chainedminds.api.friendship._Friendship;
import com.chainedminds.api.store.*;
import com.chainedminds.models._Data;
import com.chainedminds.models._ProductData;
import com.chainedminds.models.account._FriendData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.models.payment._IPGTransactionData;
import com.chainedminds.utilities._File;

public class _Resources<
        RequestHandler extends _RequestHandler<?>,
        Profile extends _Profile,
        Account extends _Account<? extends _Data<?>>,
        AccountSession extends _AccountSession,
        Friendship extends _Friendship<? extends _Data<?>, ? extends _FriendData>,
        File extends _File,
        IABPayment extends _IABPayment<? extends _IABTransactionData, ? extends _ProductData>,
        IPGPayment extends _IPGPayment<? extends _IPGTransactionData, ? extends _ProductData>,
        IABProductPurchase extends _IABProductPurchase<? extends _IABTransactionData>,
        IPGProductPurchase extends _IPGProductPurchase<? extends _IPGTransactionData>,
        IABSubscriptionPurchase extends _IABSubscriptionPurchase<? extends _IABTransactionData>,
        IPGSubscriptionPurchase extends _IPGSubscriptionPurchase<? extends _IPGTransactionData>,
        Product extends _Product<? extends _ProductData>,
        BlackList extends _BlackList> {

    private static final _Resources<
                ? extends _RequestHandler<?>,
                ? extends _Profile,
                ? extends _Account<?>,
                ? extends _AccountSession,
                ? extends _Friendship<?, ?>,
                ? extends _File,
                ? extends _IABPayment<?, ?>,
                ? extends _IPGPayment<?, ?>,
                ? extends _IABProductPurchase<?>,
                ? extends _IPGProductPurchase<?>,
                ? extends _IABSubscriptionPurchase<?>,
                ? extends _IPGSubscriptionPurchase<?>,
                ? extends _Product<?>,
                ? extends _BlackList
                > INSTANCE = new _Resources<>();

    public _Resources() {

    }

    public static <
            RequestHandler extends _RequestHandler<?>,
            Profile extends _Profile,
            Account extends _Account<? extends _Data<?>>,
            AccountSession extends _AccountSession,
            Friendship extends _Friendship<? extends _Data<?>, ? extends _FriendData>,
            File extends _File,
            IABPayment extends _IABPayment<? extends _IABTransactionData, ? extends _ProductData>,
            IPGPayment extends _IPGPayment<? extends _IPGTransactionData, ? extends _ProductData>,
            IABProductPurchase extends _IABProductPurchase<? extends _IABTransactionData>,
            IPGProductPurchase extends _IPGProductPurchase<? extends _IPGTransactionData>,
            IABSubscriptionPurchase extends _IABSubscriptionPurchase<? extends _IABTransactionData>,
            IPGSubscriptionPurchase extends _IPGSubscriptionPurchase<? extends _IPGTransactionData>,
            Product extends _Product<? extends _ProductData>,
            BlackList extends _BlackList> _Resources<
            RequestHandler, Profile, Account, AccountSession,
            Friendship, File, IABPayment, IPGPayment, IABProductPurchase,
            IPGProductPurchase, IABSubscriptionPurchase, IPGSubscriptionPurchase, Product, BlackList
            > getInstance() {

        return (_Resources<
                RequestHandler, Profile, Account, AccountSession,
                Friendship, File, IABPayment, IPGPayment, IABProductPurchase,
                IPGProductPurchase, IABSubscriptionPurchase, IPGSubscriptionPurchase, Product, BlackList
                        >) INSTANCE;
    }

    public RequestHandler requestHandler;
    public Profile profile;
    public Account account;
    public AccountSession accountSession;
    public Friendship friendship;
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