package com.chainedminds;

import com.chainedminds.api.accounting.BaseAccountManager;
import com.chainedminds.api.accounting.BaseAccountPropertyManager;
import com.chainedminds.api.accounting.BaseBlackListManager;
import com.chainedminds.api.accounting.BaseProfileManager;
import com.chainedminds.api.friendship.BaseFriendshipManager;
import com.chainedminds.api.store.*;
import com.chainedminds.models.*;
import com.chainedminds.models.account.BaseAccountData;
import com.chainedminds.models.account.BaseFriendData;
import com.chainedminds.models.notification.BaseNotificationData;
import com.chainedminds.models.payment.BaseIABTransactionData;
import com.chainedminds.models.payment.BaseIPGTransactionData;
import com.chainedminds.samples.TestRequestManager;
import com.chainedminds.utilities.BaseFileManager;

class TestResources extends BaseResources<
        TestRequestManager,
        BaseProfileManager,
        BaseAccountManager<BaseData>,
        BaseAccountPropertyManager<BaseData>,
        BaseFriendshipManager<BaseData, BaseFriendData>,
        BaseFileManager,
        BaseIABPaymentManager<BaseData, BaseIABTransactionData, BaseProductData>,
        BaseIPGPaymentManager<BaseData, BaseIPGTransactionData, BaseProductData>,
        BaseIABProductPurchasesManager<BaseIABTransactionData>,
        BaseIPGProductPurchasesManager<BaseIPGTransactionData>,
        BaseIABSubscriptionPurchasesManager<BaseIABTransactionData>,
        BaseIPGSubscriptionPurchasesManager<BaseIPGTransactionData>,
        BaseProductsManager<BaseProductData>,
        BaseBlackListManager> {

    private static final TestResources INSTANCE = new TestResources();

    public static TestResources getInstance() {

        return INSTANCE;
    }

    public void config() {

        super.requestManager = new TestRequestManager(BaseData.class);
        super.profileManager = new BaseProfileManager();
        super.accountManager = new BaseAccountManager<>();
        super.accountPropertyManager = new BaseAccountPropertyManager<>();
        super.friendshipManager = new BaseFriendshipManager<>();
        super.fileManager = new BaseFileManager();
        super.iabPaymentManager = new BaseIABPaymentManager<>();
        super.ipgPaymentManager = new BaseIPGPaymentManager<>();
        super.iabProductPurchasesManager = new BaseIABProductPurchasesManager<>();
        super.ipgProductPurchasesManager = new BaseIPGProductPurchasesManager<>();
        super.iabSubscriptionPurchasesManager = new BaseIABSubscriptionPurchasesManager<>();
        super.ipgSubscriptionPurchasesManager = new BaseIPGSubscriptionPurchasesManager<>();
        super.productManager = new BaseProductsManager<>();
        super.blackListManager = new BaseBlackListManager();

        BaseResources baseResources = BaseResources.getInstance();

        baseResources.requestManager = super.requestManager;
        baseResources.profileManager = super.profileManager;
        baseResources.accountManager = super.accountManager;
        baseResources.accountPropertyManager = super.accountPropertyManager;
        baseResources.friendshipManager = super.friendshipManager;
        baseResources.fileManager = super.fileManager;
        baseResources.iabPaymentManager = super.iabPaymentManager;
        baseResources.ipgPaymentManager = super.ipgPaymentManager;
        baseResources.iabProductPurchasesManager = super.iabProductPurchasesManager;
        baseResources.ipgProductPurchasesManager = super.ipgProductPurchasesManager;
        baseResources.iabSubscriptionPurchasesManager = super.iabSubscriptionPurchasesManager;
        baseResources.ipgSubscriptionPurchasesManager = super.ipgSubscriptionPurchasesManager;
        baseResources.productManager = super.productManager;
        baseResources.blackListManager = super.blackListManager;

        BaseClasses baseClasses = BaseClasses.getInstance();

        baseClasses.dataClass = BaseData.class;
        baseClasses.accountClass = BaseAccountData.class;
        baseClasses.friendClass = BaseFriendData.class;
        baseClasses.fileClass = BaseFileData.class;
        baseClasses.iabTransactionClass = BaseIABTransactionData.class;
        baseClasses.ipgTransactionClass = BaseIPGTransactionData.class;
        baseClasses.productClass = BaseProductData.class;
        baseClasses.messageClass = BaseMessageData.class;
        baseClasses.notificationClass = BaseNotificationData.class;
        baseClasses.newsClass = BaseNewsData.class;
        baseClasses.profileClass = BaseProfileData.class;
    }
}