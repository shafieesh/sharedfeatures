package com.chainedminds.network.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class ChannelPool {

    public static final Map<String, RequestHandler> REQUEST_HANDLERS = new HashMap<>();
    public static final Map<String, ChannelHandlerContext> CHANNELS = new HashMap<>();

    public static void registerHandler(String channelID, RequestHandler handler) {

        REQUEST_HANDLERS.put(channelID, handler);
    }

    public static void registerChannel(String channelID, ChannelHandlerContext channel) {

        CHANNELS.put(channelID, channel);
    }

    public static void removeListener(String channelID) {

        CHANNELS.remove(channelID);

        RequestHandler listener = REQUEST_HANDLERS.remove(channelID);

        if (listener != null) {

            listener.handleClose(channelID);
        }
    }

    public interface RequestHandler {

        void handleRequest(String channelID, byte[] data);

        void handleClose(String channelID);
    }
}