package com.chainedminds.network.netty.telnetPipe;

import com.chainedminds._Resources;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.network.netty.ChannelListeners;
import com.chainedminds.network.netty.NettyServer;
import io.netty.channel.*;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class TelnetChannelProcessor extends SimpleChannelInboundHandler<String> {

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
    public void channelRead0(ChannelHandlerContext context, String requestData) {

        InetSocketAddress remoteAddress = ((InetSocketAddress) context.channel().remoteAddress());

        Runnable task = () -> {

            Object responseData = _Resources.get().requestHandler
                    .processRequest(context, remoteAddress, requestData);

            if (responseData != null) {

                _RequestHandler.optimizeReadTimeout(context, responseData);

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

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {

        //cause.printStackTrace();

        context.close();
    }
}