package com.chainedminds;

import com.chainedminds.api.BaseRequestsManager;
import com.chainedminds.api.accounting.BaseAccountManager;
import com.chainedminds.api.accounting.BaseAccountPropertyManager;
import com.chainedminds.api.accounting.BaseBlackListManager;
import com.chainedminds.api.accounting.BaseProfileManager;
import com.chainedminds.api.friendship.BaseFriendshipManager;
import com.chainedminds.api.store.*;
import com.chainedminds.models.BaseData;
import com.chainedminds.models.BaseProductData;
import com.chainedminds.models.account.BaseAccountData;
import com.chainedminds.models.account.BaseFriendData;
import com.chainedminds.models.payment.BaseIABTransactionData;
import com.chainedminds.models.payment.BaseIPGTransactionData;
import com.chainedminds.utilities.BaseFileManager;

public class BaseResources<
        Data extends BaseData<BaseAccountData>,
        RequestManager extends BaseRequestsManager<?>,
        ProfileManager extends BaseProfileManager,
        AccountManager extends BaseAccountManager<Data>,
        AccountPropertyManager extends BaseAccountPropertyManager<Data>,
        FriendshipManager extends BaseFriendshipManager<Data, ? extends BaseFriendData>,
        FileManager extends BaseFileManager,
        IABPaymentManager extends BaseIABPaymentManager<Data, ? extends BaseIABTransactionData, ? extends BaseProductData>,
        IPGPaymentManager extends BaseIPGPaymentManager<Data, ? extends BaseIPGTransactionData, ? extends BaseProductData>,
        IABPurchasesManager extends BaseIABProductPurchasesManager<? extends BaseIABTransactionData>,
        IPGPurchasesManager extends BaseIPGProductPurchasesManager<? extends BaseIPGTransactionData>,
        IABSubscriptionPurchasesManager extends BaseIABSubscriptionPurchasesManager<? extends BaseIABTransactionData>,
        IPGSubscriptionPurchasesManager extends BaseIPGSubscriptionPurchasesManager<? extends BaseIPGTransactionData>,
        ProductManager extends BaseProductsManager<? extends BaseProductData>,
        BlackListManager extends BaseBlackListManager> {

    private static final BaseResources<
            ? extends BaseData<BaseAccountData>,
            ? extends BaseRequestsManager<?>,
            ? extends BaseProfileManager,
            ? extends BaseAccountManager<?>,
            ? extends BaseAccountPropertyManager<?>,
            ? extends BaseFriendshipManager<?, ?>,
            ? extends BaseFileManager,
            ? extends BaseIABPaymentManager<?, ?, ?>,
            ? extends BaseIPGPaymentManager<?, ?, ?>,
            ? extends BaseIABProductPurchasesManager<?>,
            ? extends BaseIPGProductPurchasesManager<?>,
            ? extends BaseIABSubscriptionPurchasesManager<?>,
            ? extends BaseIPGSubscriptionPurchasesManager<?>,
            ? extends BaseProductsManager<?>,
            ? extends BaseBlackListManager
            > INSTANCE = new BaseResources<>();

    public BaseResources() {

    }

    public static <
            R extends BaseRequestsManager<?>,
            ProfileManager extends BaseProfileManager,
            AccountManager extends BaseAccountManager<?>,
            AccountPropertyManager extends BaseAccountPropertyManager<?>,
            FriendshipManager extends BaseFriendshipManager<?, ?>,
            FileManager extends BaseFileManager,
            IABPaymentManager extends BaseIABPaymentManager<?, ?, ?>,
            IPGPaymentManager extends BaseIPGPaymentManager<?, ?, ?>,
            IABProductPurchasesManager extends BaseIABProductPurchasesManager<?>,
            IPGProductPurchasesManager extends BaseIPGProductPurchasesManager<?>,
            IABSubscriptionPurchasesManager extends BaseIABSubscriptionPurchasesManager<?>,
            IPGSubscriptionPurchasesManager extends BaseIPGSubscriptionPurchasesManager<?>,
            ProductsManager extends BaseProductsManager<?>,
            BlackListManager extends BaseBlackListManager
            > BaseResources<
            ?, R, ProfileManager, AccountManager, AccountPropertyManager,
            FriendshipManager, FileManager, IABPaymentManager, IPGPaymentManager, IABProductPurchasesManager,
            IPGProductPurchasesManager, IABSubscriptionPurchasesManager, IPGSubscriptionPurchasesManager, ProductsManager, BlackListManager
            > getInstance() {

        return (BaseResources<
                ?, R, ProfileManager, AccountManager, AccountPropertyManager,
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