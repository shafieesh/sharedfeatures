package com.chainedminds.network.netty.mainPipe;

import com.chainedminds._Resources;
import com.chainedminds.api._RequestHandler;
import com.chainedminds.network.netty.ChannelListeners;
import com.chainedminds.network.netty.NettyServer;
import io.netty.channel.*;

import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.Arrays;

public class MainFileProcessor extends ChannelInboundHandlerAdapter {

    public static boolean KEEP_ALIVE = false;

    private byte[] message;
    private byte[] data;

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {

        super.channelActive(context);

        ChannelFuture channelCloseFuture = context.channel().closeFuture();

        channelCloseFuture.addListener((ChannelFutureListener) future -> {

            ChannelListeners.removeListener(future.channel().id().asLongText());
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object data) {

        if (this.message == null && data instanceof byte[]) {

            this.message = (byte[]) data;

            return;
        }
        if (this.data == null && data instanceof byte[]) {

            this.data = (byte[]) data;
        }

        InetSocketAddress remoteAddress = ((InetSocketAddress) context.channel().remoteAddress());

        if (message != null && this.data != null) {

            byte[] newMessageHolder = this.message;
            byte[] newDataHolder = this.data;

            this.message = null;
            this.data = null;

            Runnable task = () -> {

                Object responseData = _Resources.get().fileHandler
                        .processRequest(context, remoteAddress, newMessageHolder, newDataHolder);

                _RequestHandler.optimizeReadTimeout(context, responseData);

                if (responseData != null) {

                    ChannelFuture writeFuture = context.channel().writeAndFlush(responseData);

                    if (!KEEP_ALIVE) {

                        writeFuture.addListener(ChannelFutureListener.CLOSE);
                    }
                }
            };

            if (!NettyServer.execute(task)) {

                Object responseData = _Resources.get().fileHandler
                        .sendServerBusyResponse(newMessageHolder, newDataHolder);

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