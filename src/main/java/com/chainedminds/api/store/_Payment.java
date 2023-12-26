package com.chainedminds.api.store;

import com.chainedminds.models._ProductData;
import com.chainedminds.models.payment._TransactionData;

import java.sql.Connection;

public class _Payment<ProductData extends _ProductData> {

    public static final int PURCHASE_STATE_REJECTED = -1;
    public static final int PURCHASE_STATE_PENDING = 0;
    public static final int PURCHASE_STATE_VERIFIED = 1;
    public static final int PURCHASE_STATE_APPLIED = 2;

    private static final String TAG = _Payment.class.getSimpleName();

    public boolean onConsuming(Connection connection, _TransactionData transaction, ProductData product) {

        return false;
    }

    public void onConsumeFinished(_TransactionData transaction, ProductData product) {

    }
}