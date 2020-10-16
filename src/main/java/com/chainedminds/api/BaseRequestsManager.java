package com.chainedminds.api;

import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.api.accounting.BaseAccountPropertyManager;
import com.chainedminds.api.friendship.BaseFriendshipManager;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.ClientData;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.json.JsonHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseRequestsManager<Data extends BaseData> {

    private static final String TAG = BaseRequestsManager.class.getSimpleName();

    private final Class<Data> mappedClass;

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final Map<Integer, Long> LAST_ACCESS_CACHE = new HashMap<>();

    public BaseRequestsManager(Class<Data> mappedClass) {

        this.mappedClass = mappedClass;
    }

    public long getLastAccessTime(int userID) {

        cleanUpLastAccessTimes();

        AtomicLong lastAccessTime = new AtomicLong();

        Utilities.lock(TAG, LOCK.readLock(), () -> lastAccessTime.set(LAST_ACCESS_CACHE.getOrDefault(userID, 0L)));

        return lastAccessTime.get();
    }

    public void setLastAccessTime(int userID, String appName) {

        long lastAccessTime = getLastAccessTime(userID);

        long currentTime = System.currentTimeMillis();

        Utilities.lock(TAG, LOCK.writeLock(), () -> LAST_ACCESS_CACHE.put(userID, currentTime));

        if (System.currentTimeMillis() - lastAccessTime >= BaseConfig.FIVE_MINUTES) {

            BaseAccountPropertyManager.USER_ACTIVITY.getOrDefault(appName, new HashMap<>()).put(userID, currentTime);

            BaseFriendshipManager.notifyPlayerIsOnline(userID, appName);
        }
    }

    private void cleanUpLastAccessTimes() {

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            LAST_ACCESS_CACHE.keySet().removeIf(key -> System.currentTimeMillis() -
                    LAST_ACCESS_CACHE.getOrDefault(key, 0L) > BaseConfig.TEN_MINUTES);
        });
    }

    public int getRecentUsersCount() {

        cleanUpLastAccessTimes();

        return LAST_ACCESS_CACHE.size();
    }

    public Object handleRequest(Data data, Socket socket) {

        BaseData responseData = new BaseData();
        responseData.response = BaseCodes.RESPONSE_NOK;
        responseData.message = "Default response from BaseRequestManager";

        return responseData;
    }

    public Object processRequest(ChannelHandlerContext channelContext, InetSocketAddress remoteAddress, Object request) {

        try {

            Data bakedRequest = prepareRequest(channelContext, remoteAddress, request);

            Object pendingObject = handleRequest(bakedRequest, null);

            return prepareResponse(request, bakedRequest.client.apiVersion, bakedRequest.client.appVersion, pendingObject);

        } catch (Exception e) {

            BaseData pendingObject = new BaseData<>();

            pendingObject.response = BaseCodes.RESPONSE_NOK;
            pendingObject.message = "The request was not handled.";

            return prepareResponse(request, 1, 0, pendingObject);
        }
    }

    public Object sendServerBusyResponse(Object request) {

        BaseData pendingObject = new BaseData<>();

        pendingObject.response = BaseCodes.RESPONSE_NOK;
        pendingObject.message = "Server is too busy. Try again later";

        return prepareResponse(request, 1, 0, pendingObject);
    }

    private Data prepareRequest(ChannelHandlerContext channelContext, InetSocketAddress remoteAddress, Object data) {

        Data request = null;

        if (data instanceof byte[]) {

            request = JsonHelper.getObject((byte[]) data, mappedClass);
        }

        if (data instanceof String) {

            request = JsonHelper.getObject((String) data, mappedClass);
        }

        if (request != null) {

            if (request.client == null) {

                request.client = new ClientData();
            }

            request.client.channelContext = channelContext;
            //request.client.channelID = channelContext.channel().id().asLongText();

            if (request.client.address == null) {

                request.client.address = remoteAddress.getAddress().getHostAddress();
            }
        }

        return request;
    }

    private static Object prepareResponse(Object originalRequest, int apiVersion, int appVersion, Object response) {

        byte[] bakedResponse;

        if (response instanceof byte[]) {

            bakedResponse = (byte[]) response;

        } else if (response instanceof String) {

            bakedResponse = ((String) response).getBytes(StandardCharsets.UTF_8);

        } else {

            bakedResponse = JsonHelper.getBytes(response);
        }

        if (originalRequest instanceof byte[]) {

            return bakedResponse;
        }

        if (originalRequest instanceof String) {

            String returningResponse = null;

            if (bakedResponse != null) {

                returningResponse = new String(bakedResponse) + "\r\n";
            }

            return returningResponse;
        }

        return null;
    }

    public static void optimizeReadTimeout(ChannelHandlerContext context, Object message) {

        int timeout = BaseConfig.DEFAULT_TIMEOUT;

        if (message instanceof byte[]) {

            timeout = 60 * ((byte[]) message).length / (300 * 1024);
        }

        if (message instanceof String) {

            timeout = 60 * ((String) message).length() / (300 * 1024);
        }

        if (message instanceof FullHttpResponse) {

            timeout = 60 * ((FullHttpResponse) message).content().readableBytes() / (300 * 1024);
        }

        if (timeout > BaseConfig.DEFAULT_TIMEOUT) {

            context.pipeline().replace("AUTO_CLOSER", "AUTO_CLOSER", new ReadTimeoutHandler(timeout));
        }
    }
}