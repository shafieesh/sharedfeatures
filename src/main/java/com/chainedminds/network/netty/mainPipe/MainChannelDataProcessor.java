package com.chainedminds.network.netty.mainPipe;

import com.chainedminds.BaseResources;
import com.chainedminds.api.BaseRequestsManager;
import com.chainedminds.network.netty.ChannelListeners;
import com.chainedminds.network.netty.NettyServer;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class MainChannelDataProcessor extends ChannelInboundHandlerAdapter {

    private static int newConnectionsCount = 0;

    public static int getNewConnectionsCount() {

        int copiedNewConnectionsCount = newConnectionsCount;

        newConnectionsCount = 0;

        return copiedNewConnectionsCount;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) throws Exception {

        super.channelActive(context);

        newConnectionsCount++;

        ChannelFuture channelCloseFuture = context.channel().closeFuture();

        channelCloseFuture.addListener((ChannelFutureListener) future -> {

            ChannelListeners.removeListener(future.channel().id().asLongText());
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext context, @NotNull Object message) {

        byte[] requestData = (byte[]) message;

        String channelID = context.channel().id().asLongText();

        InetSocketAddress remoteAddress = ((InetSocketAddress) context.channel().remoteAddress());

        if (ChannelListeners.REGISTERED_PLAYERS.containsKey(channelID)) {

            ChannelListeners.REGISTERED_PLAYERS.get(channelID).onRequestReceived(channelID, requestData);

        } else {

            Runnable task = () -> {

                Object responseData = BaseResources.getInstance().requestManager
                        .processRequest(context, remoteAddress, requestData);

                BaseRequestsManager.optimizeReadTimeout(context, responseData);

                ChannelFuture writeFuture = context.channel().writeAndFlush(responseData);

                if (NettyServer.KEEP_ALIVE_CHANNELS_LIST.contains(channelID)) {

                    NettyServer.KEEP_ALIVE_CHANNELS_LIST.remove(channelID);

                } else {

                    //writeFuture.addListener(ChannelFutureListener.CLOSE);
                }
            };

            if (!NettyServer.execute(task)) {

                Object responseData = BaseResources.getInstance().requestManager
                        .sendServerBusyResponse(requestData);

                BaseRequestsManager.optimizeReadTimeout(context, responseData);

                context.channel().writeAndFlush(responseData);//.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {

        //cause.printStackTrace();

        context.close();
    }
}