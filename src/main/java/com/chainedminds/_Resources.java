package com.chainedminds;

import com.chainedminds.api._RequestHandler;
import com.chainedminds.api.accounting._AccountManager;
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
        RequestManager extends _RequestHandler<?>,
        ProfileManager extends _Profile,
        AccountManager extends _AccountManager<? extends _Data<?>>,
        AccountPropertyManager extends _AccountSession,
        FriendshipManager extends _Friendship<? extends _Data<?>, ? extends _FriendData>,
        FileManager extends _File,
        IABPaymentManager extends _IABPayment<? extends _IABTransactionData, ? extends _ProductData>,
        IPGPaymentManager extends _IPGPayment<? extends _IPGTransactionData, ? extends _ProductData>,
        IABPurchasesManager extends _IABProductPurchase<? extends _IABTransactionData>,
        IPGPurchasesManager extends _IPGProductPurchase<? extends _IPGTransactionData>,
        IABSubscriptionPurchasesManager extends _IABSubscriptionPurchase<? extends _IABTransactionData>,
        IPGSubscriptionPurchasesManager extends _IPGSubscriptionPurchase<? extends _IPGTransactionData>,
        ProductManager extends _Product<? extends _ProductData>,
        BlackListManager extends _BlackList> {

    private static final _Resources<
                ? extends _RequestHandler<?>,
                ? extends _Profile,
                ? extends _AccountManager<?>,
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
            RequestManager extends _RequestHandler<?>,
            ProfileManager extends _Profile,
            AccountManager extends _AccountManager<?>,
            AccountPropertyManager extends _AccountSession,
            FriendshipManager extends _Friendship<?, ?>,
            FileManager extends _File,
            IABPaymentManager extends _IABPayment<?, ?>,
            IPGPaymentManager extends _IPGPayment<?, ?>,
            IABProductPurchasesManager extends _IABProductPurchase<?>,
            IPGProductPurchasesManager extends _IPGProductPurchase<?>,
            IABSubscriptionPurchasesManager extends _IABSubscriptionPurchase<?>,
            IPGSubscriptionPurchasesManager extends _IPGSubscriptionPurchase<?>,
            ProductsManager extends _Product<?>,
            BlackListManager extends _BlackList
            > _Resources<
                        RequestManager, ProfileManager, AccountManager, AccountPropertyManager,
                        FriendshipManager, FileManager, IABPaymentManager, IPGPaymentManager, IABProductPurchasesManager,
                        IPGProductPurchasesManager, IABSubscriptionPurchasesManager, IPGSubscriptionPurchasesManager, ProductsManager, BlackListManager
                        > getInstance() {

        return (_Resources<
                        RequestManager, ProfileManager, AccountManager, AccountPropertyManager,
                        FriendshipManager, FileManager, IABPaymentManager, IPGPaymentManager, IABProductPurchasesManager,
                        IPGProductPurchasesManager, IABSubscriptionPurchasesManager, IPGSubscriptionPurchasesManager, ProductsManager, BlackListManager
                        >) INSTANCE;
    }

    public RequestManager requestManager;
    public ProfileManager profileManager;
    public AccountManager accountManager;
    public AccountPropertyManager accountPropertyManager;
    public FriendshipManager friendshipManager;
    public FileManager fileManager;
    public IABPaymentManager iabPaymentManager;
    public IPGPaymentManager ipgPaymentManager;
    public IABPurchasesManager iabProductPurchasesManager;
    public IPGPurchasesManager ipgProductPurchasesManager;
    public IABSubscriptionPurchasesManager iabSubscriptionPurchasesManager;
    public IPGSubscriptionPurchasesManager ipgSubscriptionPurchasesManager;
    public ProductManager productManager;
    public BlackListManager blackListManager;
}