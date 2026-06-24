package com.chainedminds.api;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds.models.SmallData;
import com.chainedminds.utilities._Log;
import com.chainedminds.utilities.json.Json;
import com.chainedminds.utilities.json.JsonException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class _FileHandler<Data> {

    private static final String TAG = _FileHandler.class.getSimpleName();

    private final Class<Data> mappedClass;

    public _FileHandler(Class<Data> mappedClass) {

        this.mappedClass = mappedClass;
    }

    public Object handleRequest(Data request, byte[] data) {

        SmallData response = new SmallData();
        response.response = _Codes.RESPONSE_NOK;
        response.message = "FileHandler has not implemented";

        return response;
    }

    public Object processRequest(ChannelHandlerContext channelContext, InetSocketAddress remoteAddress, Object request, byte[] data) {

        try {

            Wrapper bakedRequest = prepareRequest(channelContext, remoteAddress, request, data);

            if (bakedRequest != null) {

                Object pendingObject = handleRequest(bakedRequest.request, bakedRequest.data);

                return prepareResponse(request, pendingObject);

            } else {

                SmallData response = new SmallData();
                response.response = _Codes.RESPONSE_NOK;
                response.message = "Could not decode request.";

                return prepareResponse(request, response);
            }

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        SmallData response = new SmallData();
        response.response = _Codes.RESPONSE_NOK;
        response.message = "The request was not handled.";

        return prepareResponse(request, response);
    }

    public Object sendServerBusyResponse(Object request, byte[] data) {

        SmallData response = new SmallData();
        response.response = _Codes.RESPONSE_NOK;
        response.message = "Server is busy. Try again later.";

        return prepareResponse(request, response);
    }

    private Wrapper prepareRequest(ChannelHandlerContext channelContext, InetSocketAddress remoteAddress, Object receivedRequest, Object receivedData) {

        Data request = null;
        byte[] data = null;

        try {

            if (receivedRequest instanceof byte[]) {

                request = Json.getObjectUnsafe((byte[]) receivedRequest, mappedClass);
            }

            if (receivedRequest instanceof String) {

                request = Json.getObjectUnsafe((String) receivedRequest, mappedClass);
            }

        } catch (JsonException ignore) {

        } catch (Exception e) {

            System.err.println("BaseFileHandler : " + e.getMessage());
        }

        if (receivedData instanceof byte[]) {

            data = (byte[]) receivedData;
        }

        if (request != null) {

            Wrapper wrapper = new Wrapper();
            wrapper.channelContext = channelContext;
            wrapper.channelID = channelContext.channel().id().asLongText();
            wrapper.address = remoteAddress.getAddress().getHostAddress();
            wrapper.request = request;
            wrapper.data = data;

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
        public Data request;
        public byte[] data;
    }
}