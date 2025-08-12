package com.chainedminds.api.store;

import com.chainedminds._Classes;
import com.chainedminds._Config;
import com.chainedminds.models._ProductData;
import com.chainedminds.utilities.Task;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _Product<ProductData extends _ProductData> {

    private static final String TAG = _Product.class.getSimpleName();

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

    protected final List<ProductData> PRODUCTS = new ArrayList<>();

    protected static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start() {

        Task.add(Task.Data.build()
                .setName("ProductManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .onEachCycle(this::fetch)
                .runNow()
                .schedule());
    }

    public void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PRODUCTS +
                " ORDER BY " + FIELD_CATEGORY + " = ? DESC, " + FIELD_CATEGORY +
                " = ? DESC, " + FIELD_CATEGORY + " = ? DESC, "+ FIELD_TIER + " DESC";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, "ticket");
        parameters.put(2, "video_ad");
        parameters.put(3, "subscription");

        _DatabaseOld.query(TAG, selectStatement, parameters, new TwoStepQueryCallback() {

            private final List<ProductData> products = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    ProductData product = (ProductData) _Classes.construct(_Classes.getInstance().productClass);
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

    /*public Data getStoreProducts(Data data) {

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

                        *//*case "ticket":

                            if (appVersion >= BaseConfig.TICKET_APP_VERSION) {

                                data.products.add( product);
                            }
                            break;*//*

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
    }*/

    public boolean replaceProduct(String name, String appName, String market, String sku, String type, String category,
                                  boolean consumable, int tier, int coins, float price, int discount,
                                  boolean available) {

        if (name == null || appName == null || market == null || sku == null || type == null || category == null) {

            return false;
        }

        ProductData oldProduct = getProduct(market, sku);

        int id = 0;

        if (oldProduct != null) {

            id = oldProduct.id;
        }

        String statement;
        Map<Integer, Object> parameters = new HashMap<>();

        if (id == 0) {

            parameters.put(1, name);
            parameters.put(2, appName);
            parameters.put(3, market);
            parameters.put(4, sku);
            parameters.put(5, type);
            parameters.put(6, category);
            parameters.put(7, consumable);
            parameters.put(8, tier);
            parameters.put(9, coins);
            parameters.put(10, price);
            parameters.put(11, discount);
            parameters.put(12, available);

            statement = "INSERT INTO " + _Config.TABLE_PRODUCTS +
                    " (" + FIELD_NAME + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET + ", " +
                    FIELD_SKU + ", " + FIELD_TYPE + ", " + FIELD_CATEGORY + ", " + FIELD_CONSUMABLE + ", " + FIELD_TIER +
                    ", " + FIELD_COINS + ", " + FIELD_PRICE + ", " + FIELD_DISCOUNT + ", " + FIELD_AVAILABILITY +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + FIELD_NAME + " = Values(" +
                    FIELD_NAME + "), " + FIELD_APP_NAME + " = Values(" + FIELD_APP_NAME + "), " + FIELD_MARKET +
                    " = Values(" + FIELD_MARKET + "), " + FIELD_SKU + " = Values(" + FIELD_SKU + "), " + FIELD_TYPE
                    + " = Values(" + FIELD_TYPE + "), " + FIELD_CATEGORY + " = Values(" + FIELD_CATEGORY +
                    "), " + FIELD_CONSUMABLE + " = Values(" + FIELD_CONSUMABLE + "), " + FIELD_TIER + " = Values(" +
                    FIELD_TIER + "), " + FIELD_COINS + " = Values(" + FIELD_COINS + "), " + FIELD_PRICE + " = Values(" +
                    FIELD_PRICE + "), " + FIELD_DISCOUNT + " = Values(" + FIELD_DISCOUNT + "), " + FIELD_AVAILABILITY +
                    " = Values(" + FIELD_AVAILABILITY + ")";

        } else {

            parameters.put(1, id);
            parameters.put(2, name);
            parameters.put(3, appName);
            parameters.put(4, market);
            parameters.put(5, sku);
            parameters.put(6, type);
            parameters.put(7, category);
            parameters.put(8, consumable);
            parameters.put(9, tier);
            parameters.put(10, coins);
            parameters.put(11, price);
            parameters.put(12, discount);
            parameters.put(13, available);

            statement = "INSERT INTO " + _Config.TABLE_PRODUCTS +
                    " (" + FIELD_ID + ", " + FIELD_NAME + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET + ", " +
                    FIELD_SKU + ", " + FIELD_TYPE + ", " + FIELD_CATEGORY + ", " + FIELD_CONSUMABLE + ", " + FIELD_TIER
                    + ", " + FIELD_COINS + ", " + FIELD_PRICE + ", " + FIELD_DISCOUNT + ", " + FIELD_AVAILABILITY +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + FIELD_ID +
                    " = Values(" + FIELD_ID + "), " + FIELD_NAME + " = Values(" + FIELD_NAME + "), " + FIELD_APP_NAME +
                    " = Values(" + FIELD_APP_NAME + "), " + FIELD_MARKET + " = Values(" + FIELD_MARKET + "), " +
                    FIELD_SKU + " = Values(" + FIELD_SKU + "), " + FIELD_TYPE + " = Values(" + FIELD_TYPE + "), " +
                    FIELD_CATEGORY + " = Values(" + FIELD_CATEGORY + "), " + FIELD_CONSUMABLE + " = Values(" +
                    FIELD_CONSUMABLE + "), " + FIELD_TIER + " = Values(" + FIELD_TIER + "), " + FIELD_COINS +
                    " = Values(" + FIELD_COINS + "), " + FIELD_PRICE + " = Values(" + FIELD_PRICE + "), " +
                    FIELD_DISCOUNT + " = Values(" + FIELD_DISCOUNT + "), " + FIELD_AVAILABILITY +
                    " = Values(" + FIELD_AVAILABILITY + ")";
        }

        return _DatabaseOld.insert(TAG, statement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                fetch();

            } else {

                error.printStackTrace();
            }
        });
    }

    public boolean replaceProduct(ProductData product) {

        if (product == null || product.name == null || product.appName == null || product.market == null ||
                product.sku == null || product.type == null || product.category == null) {

            return false;
        }

        String name = product.name;
        String appName = product.appName;
        String market = product.market;
        String sku = product.sku;
        String type = product.type;
        String category = product.category;
        boolean consumable = product.consumable;
        int tier = product.tier;
        int coins = product.coins;
        float price = product.price;
        int discount = product.discount;
        boolean available = product.available;

        return replaceProduct(name, appName, market, sku, type, category, consumable, tier, coins, price, discount,
                available);
    }

    public boolean removeProduct(String appName, String sku) {

        if (appName == null || sku == null) {

            return false;
        }

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, appName);
        parameters.put(2, sku);

        String statement = "DELETE FROM " + _Config.TABLE_PRODUCTS + " WHERE " + FIELD_APP_NAME +
                " = ? AND " + FIELD_SKU + " = ?";

        return _DatabaseOld.update(TAG, statement, parameters, (wasSuccessful, error) -> {

            if (wasSuccessful) {

                fetch();

            } else {

                error.printStackTrace();
            }
        });
    }
}