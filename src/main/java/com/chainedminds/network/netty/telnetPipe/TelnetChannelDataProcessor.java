package com.chainedminds.network.netty.telnetPipe;

import com.chainedminds._Resources;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.network.netty.ChannelListeners;
import com.chainedminds.network.netty.NettyServer;
import io.netty.channel.*;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public
class TelnetChannelDataProcessor extends SimpleChannelInboundHandler<String> {

    private static int newConnectionsCount = 0;

    public static int getNewConnectionsCount() {

        int copiedNewConnectionsCount = newConnectionsCount;

        newConnectionsCount = 0;

        return copiedNewConnectionsCount;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {

        super.channelActive(context);

        newConnectionsCount++;

        ChannelFuture channelCloseFuture = context.channel().closeFuture();

        channelCloseFuture.addListener((ChannelFutureListener) future -> {

            ChannelListeners.removeListener(future.channel().id().asLongText());
        });
    }

    @Override
    public void channelRead0(ChannelHandlerContext context, String requestData) {

        String channelID = context.channel().id().asLongText();

        InetSocketAddress remoteAddress = ((InetSocketAddress) context.channel().remoteAddress());

        Runnable task = () -> {

            Object responseData = _Resources.getInstance().requestManager
                    .processRequest(context, remoteAddress, requestData);

            if (responseData != null) {

                _RequestHandler.optimizeReadTimeout(context, responseData);

                ChannelFuture writeFuture = context.channel().writeAndFlush(responseData);
            }

            if (NettyServer.KEEP_ALIVE_CHANNELS_LIST.contains(channelID)) {

                NettyServer.KEEP_ALIVE_CHANNELS_LIST.remove(channelID);

            } else {

                //writeFuture.addListener(ChannelFutureListener.CLOSE);
            }
        };

        if (!NettyServer.execute(task)) {

            Object responseData = _Resources.getInstance().requestManager
                    .sendServerBusyResponse(requestData);

            _RequestHandler.optimizeReadTimeout(context, responseData);

            context.channel().writeAndFlush(responseData);//.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {

        //cause.printStackTrace();

        context.close();
    }
}