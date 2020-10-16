package com.chainedminds.network.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class ChannelListeners {

    public static final Map<String, GameRequestListener> REGISTERED_PLAYERS = new HashMap<>();

    public static void registerListener(ChannelHandlerContext context, GameRequestListener listener) {

        REGISTERED_PLAYERS.put(context.channel().id().asLongText(), listener);
    }

    public static void removeListener(String channelID) {

        GameRequestListener listener = REGISTERED_PLAYERS.get(channelID);

        if (listener != null) {

            listener.onConnectionClosed(channelID);
        }

        REGISTERED_PLAYERS.remove(channelID);
    }

    public interface GameRequestListener {

        void onRequestReceived(String channelID, byte[] data);

        void onConnectionClosed(String channelID);
    }
}