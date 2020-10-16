package com.chainedminds.api.store;

import com.chainedminds.dataClasses.BaseProductData;
import com.chainedminds.dataClasses.payment.BaseTransactionData;

import java.sql.Connection;

public class BasePaymentManager<ProductData extends BaseProductData> {

    public static final int PURCHASE_STATE_REJECTED = -1;
    public static final int PURCHASE_STATE_PENDING = 0;
    public static final int PURCHASE_STATE_VERIFIED = 1;
    public static final int PURCHASE_STATE_APPLIED = 2;

    private static final String TAG = BasePaymentManager.class.getSimpleName();

    public boolean onConsuming(Connection connection, BaseTransactionData transaction, ProductData product) {

        return false;
    }

    public void onConsumeFinished(BaseTransactionData transaction, ProductData product) {

    }
}