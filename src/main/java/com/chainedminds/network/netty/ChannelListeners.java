package com.chainedminds.network.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class ChannelListeners {

    public static final Map<String, RequestListener> REGISTERED_CLIENTS = new HashMap<>();

    public static void registerListener(ChannelHandlerContext context, RequestListener listener) {

        REGISTERED_CLIENTS.put(context.channel().id().asLongText(), listener);
    }

    public static void removeListener(String channelID) {

        RequestListener listener = REGISTERED_CLIENTS.get(channelID);

        if (listener != null) {

            listener.onConnectionClosed(channelID);
        }

        REGISTERED_CLIENTS.remove(channelID);
    }

    public interface RequestListener {

        void onRequestReceived(String channelID, byte[] data);

        void onConnectionClosed(String channelID);
    }
}