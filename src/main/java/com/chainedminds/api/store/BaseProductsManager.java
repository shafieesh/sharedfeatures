package com.chainedminds.api.store;

import com.chainedminds.BaseClasses;
import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.BaseProductData;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.DatabaseHelper;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseProductsManager<Data extends BaseData, ProductData extends BaseProductData> {

    private static final String TAG = BaseProductsManager.class.getSimpleName();

    public static final String CATEGORY_PRODUCT = "product";
    public static final String CATEGORY_SUBSCRIPTION = "subscription";
    public static final String CATEGORY_TICKET = "ticket";
    public static final String CATEGORY_VIDEO_AD = "video_ad";

    private static final String FIELD_AVAILABILITY = "Available";
    private static final String FIELD_MARKET = "Market";
    private static final String FIELD_ID = "ID";
    private static final String FIELD_NAME = "Name";
    private static final String FIELD_TIER = "Tier";
    private static final String FIELD_COINS = "Coins";
    private static final String FIELD_PRICE = "Price";
    private static final String FIELD_DISCOUNT = "Discount";
    private static final String FIELD_TYPE = "Type";
    private static final String FIELD_CATEGORY = "Category";
    private static final String FIELD_CONSUMABLE = "Consumable";
    private static final String FIELD_SKU = "SKU";
    private static final String FIELD_APP_NAME = "AppName";

    private final List<ProductData> PRODUCTS = new ArrayList<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("ProductManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .setTimingListener(task -> fetch())
                .startAndSchedule());
    }

    public void fetch() {

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PRODUCTS +
                " ORDER BY " + FIELD_CATEGORY + " = ? DESC, " + FIELD_CATEGORY +
                " = ? DESC, " + FIELD_CATEGORY + " = ? DESC, "+ FIELD_TIER + " DESC";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, "ticket");
        parameters.put(2, "video_ad");
        parameters.put(3, "subscription");

        DatabaseHelper.query(TAG, selectStatement, parameters, new TwoStepQueryCallback() {

            private final List<ProductData> products = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    ProductData product = (ProductData) BaseClasses.construct(BaseClasses.getInstance().productClass);
                    product.id = resultSet.getInt(FIELD_ID);
                    product.name = resultSet.getString(FIELD_NAME);
                    product.tier = resultSet.getInt(FIELD_TIER);
                    product.coins = resultSet.getInt(FIELD_COINS);
                    product.price = resultSet.getFloat(FIELD_PRICE);
                    product.discount = resultSet.getInt(FIELD_DISCOUNT);
                    product.sku = resultSet.getString(FIELD_SKU);
                    product.type = resultSet.getString(FIELD_TYPE);
                    product.category = resultSet.getString(FIELD_CATEGORY);
                    product.consumable = resultSet.getBoolean(FIELD_CONSUMABLE);
                    product.market = resultSet.getString(FIELD_MARKET);
                    product.available = resultSet.getBoolean(FIELD_AVAILABILITY);
                    product.appName = resultSet.getString(FIELD_APP_NAME);

                    products.add(product);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        PRODUCTS.clear();

                        PRODUCTS.addAll(products);
                    });
                }
            }
        });
    }

    public float getCustomizationProductPrice(String sku) {

        AtomicReference<Float> productPrice = new AtomicReference<>(0f);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (ProductData product : PRODUCTS) {

                if (product.sku.equals(sku) && product.available) {

                    productPrice.set(product.price);

                    break;
                }
            }
        });

        return productPrice.get();
    }

    public ProductData getProduct(String market, String sku) {

        AtomicReference<ProductData> product = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (ProductData loopingProduct : PRODUCTS) {

                if (loopingProduct.market.equals(market) &&
                        loopingProduct.sku.equals(sku) &&
                        loopingProduct.available) {

                    product.set(loopingProduct);

                    break;
                }
            }
        });

        return product.get();
    }

    public ProductData getProduct(int id) {

        AtomicReference<ProductData> product = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (ProductData loopingProduct : PRODUCTS) {

                if (loopingProduct.id == id) {

                    product.set(loopingProduct);

                    break;
                }
            }
        });

        return product.get();
    }

    public Data getStoreProducts(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int appVersion = data.client.appVersion;
        int apiVersion = data.client.apiVersion;
        String appName = data.client.appName;
        String market = data.client.market;

        data.products = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (ProductData product : PRODUCTS) {

                if (product.appName.equals(appName) && product.market.equals(market)) {

                    switch (product.category) {

                        /*case "ticket":

                            if (appVersion >= BaseConfig.TICKET_APP_VERSION) {

                                data.products.add( product);
                            }
                            break;*/

                        case "subscription":

                            data.products.add(product);

                            break;

                        case "video_ad":

                            if (appVersion >= 37 || apiVersion >= 1) {

                                data.products.add( product);
                            }

                            break;

                        default:

                            if (apiVersion == 0) {

                                ProductData clonedProduct = (ProductData) BaseClasses
                                        .construct(BaseClasses.getInstance().productClass);

                                clonedProduct.id = product.id;
                                clonedProduct.name = product.name;
                                clonedProduct.tier = product.tier;
                                clonedProduct.coins = product.coins;
                                clonedProduct.price = product.price;
                                clonedProduct.discount = product.discount;
                                clonedProduct.sku = product.sku;
                                clonedProduct.category = product.category;
                                clonedProduct.consumable = product.consumable;
                                clonedProduct.market = product.market;
                                clonedProduct.available = product.available;

                                if (clonedProduct.consumable) {

                                    clonedProduct.category = BaseIABPaymentManager.PRODUCT_CATEGORY_CONSUMABLE;

                                } else {

                                    clonedProduct.category = BaseIABPaymentManager.PRODUCT_CATEGORY_NON_CONSUMABLE;
                                }

                                data.products.add(clonedProduct);
                            }

                            if (apiVersion >= 1) {

                                data.products.add(product);
                            }

                            break;
                    }
                }
            }
        });

        data.response = BaseCodes.RESPONSE_OK;

        return data;
    }
}