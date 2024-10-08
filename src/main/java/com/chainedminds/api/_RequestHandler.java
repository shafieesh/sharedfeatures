package com.chainedminds.api;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds.api.accounting._AccountSession;
import com.chainedminds.api.friendship._Friendship;
import com.chainedminds.models._Data;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities._Log;
import com.chainedminds.utilities.json.JsonException;
import com.chainedminds.utilities.json.Json;
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

public class _RequestHandler<Data> {

    private static final String TAG = _RequestHandler.class.getSimpleName();

    private final Class<Data> mappedClass;

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final Map<Integer, Long> LAST_ACCESS_CACHE = new HashMap<>();

    public _RequestHandler(Class<Data> mappedClass) {

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

        if (System.currentTimeMillis() - lastAccessTime >= _Config.FIVE_MINUTES) {

            _AccountSession.USER_ACTIVITY.getOrDefault(appName, new HashMap<>()).put(userID, currentTime);

            _Friendship.notifyPlayerIsOnline(userID, appName);
        }
    }

    private void cleanUpLastAccessTimes() {

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            LAST_ACCESS_CACHE.keySet().removeIf(key -> System.currentTimeMillis() -
                    LAST_ACCESS_CACHE.getOrDefault(key, 0L) > _Config.TEN_MINUTES);
        });
    }

    public int getRecentUsersCount() {

        cleanUpLastAccessTimes();

        return LAST_ACCESS_CACHE.size();
    }

    public Object handleRequest(Data data, Socket socket) {

        _Data<_AccountData> responseData = new _Data<>();
        responseData.response = _Codes.RESPONSE_NOK;
        responseData.message = "Default response from BaseRequestManager";

        return responseData;
    }

    public Object processRequest(ChannelHandlerContext channelContext, InetSocketAddress remoteAddress, Object request) {

        try {

            Wrapper bakedRequest = prepareRequest(channelContext, remoteAddress, request);

            if (bakedRequest != null) {

                Object pendingObject = handleRequest(bakedRequest.data, null);

                return prepareResponse(request, pendingObject);

            } else {

                _Data pendingObject = new _Data<>();

                pendingObject.response = _Codes.RESPONSE_NOK;
                pendingObject.message = "The request was not handled. Could not parse request.";

                return prepareResponse(request, pendingObject);
            }

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        _Data pendingObject = new _Data<>();

        pendingObject.response = _Codes.RESPONSE_NOK;
        pendingObject.message = "The request was not handled.";

        return prepareResponse(request, pendingObject);
    }

    public Object sendServerBusyResponse(Object request) {

        _Data pendingObject = new _Data<>();

        pendingObject.response = _Codes.RESPONSE_NOK;
        pendingObject.message = "Server is too busy. Try again later";

        return prepareResponse(request, pendingObject);
    }

    private Wrapper prepareRequest(ChannelHandlerContext channelContext, InetSocketAddress remoteAddress, Object data) {

        Data request = null;

        try {

            if (data instanceof byte[]) {

                request = Json.getObjectUnsafe((byte[]) data, mappedClass);
            }

            if (data instanceof String) {

                request = Json.getObjectUnsafe((String) data, mappedClass);
            }

        } catch (JsonException ignore) {

        } catch (Exception e) {

            System.err.println("BaseRequestManager : " + e.getMessage());
        }

        if (request != null) {

            Wrapper wrapper = new Wrapper();
            wrapper.channelContext = channelContext;
            wrapper.channelID = channelContext.channel().id().asLongText();
            wrapper.address = remoteAddress.getAddress().getHostAddress();
            wrapper.data = request;

            return wrapper;
        }

        return null;
    }

    private static Object prepareResponse(Object originalRequest, Object response) {

        byte[] bakedResponse;

        if (response instanceof byte[]) {

            bakedResponse = (byte[]) response;

        } else if (response instanceof String) {

            bakedResponse = ((String) response).getBytes(StandardCharsets.UTF_8);

        } else {

            bakedResponse = Json.getBytes(response);
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

        int timeout = _Config.DEFAULT_TIMEOUT;

        if (message instanceof byte[]) {

            timeout = 60 * ((byte[]) message).length / (300 * 1024);
        }

        if (message instanceof String) {

            timeout = 60 * ((String) message).length() / (300 * 1024);
        }

        if (message instanceof FullHttpResponse) {

            timeout = 60 * ((FullHttpResponse) message).content().readableBytes() / (300 * 1024);
        }

        if (timeout > _Config.DEFAULT_TIMEOUT) {

            context.pipeline().replace("AUTO_CLOSER", "AUTO_CLOSER", new ReadTimeoutHandler(timeout));
        }
    }

    public class Wrapper {

        public ChannelHandlerContext channelContext;
        public String channelID;
        public String address;
        public Data data;
    }
}