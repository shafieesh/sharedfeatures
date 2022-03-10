package com.chainedminds;

import com.chainedminds.api.BaseLeaderboardManager;
import com.chainedminds.api.BaseRequestsManager;
import com.chainedminds.api.accounting.BaseAccountManager;
import com.chainedminds.api.accounting.BaseAccountPropertyManager;
import com.chainedminds.api.accounting.BaseBlackListManager;
import com.chainedminds.api.accounting.BaseProfileManager;
import com.chainedminds.api.friendship.BaseFriendshipManager;
import com.chainedminds.api.store.*;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.BaseProductData;
import com.chainedminds.dataClasses.account.BaseAccountData;
import com.chainedminds.dataClasses.account.BaseFriendData;
import com.chainedminds.dataClasses.payment.BaseIABTransactionData;
import com.chainedminds.dataClasses.payment.BaseIPGTransactionData;
import com.chainedminds.utilities.BaseFileManager;

public class BaseResources<
        RequestManager extends BaseRequestsManager<? extends BaseData>,
        ProfileManager extends BaseProfileManager,
        AccountManager extends BaseAccountManager<? extends BaseData>,
        AccountPropertyManager extends BaseAccountPropertyManager<? extends BaseData>,
        FriendshipManager extends BaseFriendshipManager<? extends BaseData, ? extends BaseFriendData>,
        FileManager extends BaseFileManager,
        IABPaymentManager extends BaseIABPaymentManager<? extends BaseData, ? extends BaseIABTransactionData, ? extends BaseProductData>,
        IPGPaymentManager extends BaseIPGPaymentManager<? extends BaseData, ? extends BaseIPGTransactionData, ? extends BaseProductData>,
        IABPurchasesManager extends BaseIABProductPurchasesManager<? extends BaseIABTransactionData>,
        IPGPurchasesManager extends BaseIPGProductPurchasesManager<? extends BaseIPGTransactionData>,
        IABSubscriptionPurchasesManager extends BaseIABSubscriptionPurchasesManager<? extends BaseIABTransactionData>,
        IPGSubscriptionPurchasesManager extends BaseIPGSubscriptionPurchasesManager<? extends BaseIPGTransactionData>,
        ProductManager extends BaseProductsManager<? extends BaseProductData>,
        BlackListManager extends BaseBlackListManager,
        LeaderboardManager extends BaseLeaderboardManager<? extends BaseData, ? extends BaseAccountData>> {

    private static final String TAG = BaseResources.class.getSimpleName();

    private static final BaseResources INSTANCE = new BaseResources();

    public BaseResources() {

    }

    public static BaseResources getInstance() {

        return INSTANCE;
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
    public LeaderboardManager leaderboardManager;
}