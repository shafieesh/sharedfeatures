package com.chainedminds.network.netty.mainPipe;

import com.chainedminds._Resources;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.network.netty.ChannelListeners;
import com.chainedminds.network.netty.NettyServer;
import io.netty.channel.*;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class MainChannelProcessor extends ChannelInboundHandlerAdapter {

    public static boolean KEEP_ALIVE = false;
    private static int NEW_CONNECTIONS = 0;

    public static int getNewConnections() {

        int copiedNewConnectionsCount = NEW_CONNECTIONS;

        NEW_CONNECTIONS = 0;

        return copiedNewConnectionsCount;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {

        super.channelActive(context);

        NEW_CONNECTIONS++;

        ChannelFuture channelCloseFuture = context.channel().closeFuture();

        channelCloseFuture.addListener((ChannelFutureListener) future -> {

            ChannelListeners.removeListener(future.channel().id().asLongText());
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) {

        byte[] requestData = (byte[]) message;

        String channelID = context.channel().id().asLongText();

        InetSocketAddress remoteAddress = ((InetSocketAddress) context.channel().remoteAddress());

        if (ChannelListeners.REGISTERED_CLIENTS.containsKey(channelID)) {

            ChannelListeners.REGISTERED_CLIENTS.get(channelID).onRequestReceived(channelID, requestData);

        } else {

            Runnable task = () -> {

                Object responseData = _Resources.get().requestHandler
                        .processRequest(context, remoteAddress, requestData);

                _RequestHandler.optimizeReadTimeout(context, responseData);

                if (responseData != null) {

                    ChannelFuture writeFuture = context.channel().writeAndFlush(responseData);

                    if (!KEEP_ALIVE) {

                        writeFuture.addListener(ChannelFutureListener.CLOSE);
                    }
                }
            };

            if (!NettyServer.execute(task)) {

                Object responseData = _Resources.get().requestHandler
                        .sendServerBusyResponse(requestData);

                _RequestHandler.optimizeReadTimeout(context, responseData);

                ChannelFuture writeFuture = context.channel().writeAndFlush(responseData);

                if (!KEEP_ALIVE) {

                    writeFuture.addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {

        //cause.printStackTrace();

        context.close();
    }
}