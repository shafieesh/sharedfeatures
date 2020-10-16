package com.chainedminds.network.netty;

import com.chainedminds.network.netty.mainPipe.MainChannelDataProcessor;
import com.chainedminds.network.netty.mainPipe.MainPipeServer;
import com.chainedminds.network.netty.telnetPipe.TelnetChannelDataProcessor;
import com.chainedminds.network.netty.telnetPipe.TelnetPipeServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyServer {

    //public static final Map<String, ChannelHandlerContext> CHANNELS_MAP = new HashMap<>();
    public static final Set<String> KEEP_ALIVE_CHANNELS_LIST = new HashSet<>();

    private static final ThreadPoolExecutor REQUESTS_EXECUTOR = new ThreadPoolExecutor(
            0, 128, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

    static {

        REQUESTS_EXECUTOR.setKeepAliveTime(30L, TimeUnit.SECONDS);
        REQUESTS_EXECUTOR.allowCoreThreadTimeOut(true);
    }

    public static void start() {

        EventLoopGroup connectionExecutor = new NioEventLoopGroup();
        EventLoopGroup ioExecutor = new NioEventLoopGroup();

        MainPipeServer.start(connectionExecutor, ioExecutor);
        TelnetPipeServer.start(connectionExecutor, ioExecutor);
    }

    public static boolean execute(Runnable runnable) {

        try {

            NettyServer.REQUESTS_EXECUTOR.execute(runnable);

            return true;

        } catch (Exception ignore) {

            return false;
        }
    }

    /*public static ChannelFuture writeMessage(String channelID, boolean keepAlive, Object message) {

        try {

            ChannelHandlerContext channelHandlerContext = CHANNELS_MAP.get(channelID);

            if (channelHandlerContext != null) {

                int timeout = BaseConfig.DEFAULT_TIMEOUT;

                if (message instanceof byte[]) {

                    timeout = 60 * ((byte[]) message).length / (300 * 1024);
                }

                if (message instanceof String) {

                    timeout = 60 * ((String) message).length() / (300 * 1024);
                }

                if (message instanceof FullHttpResponse) {

                    timeout = 60 * ((FullHttpResponse) message).content().readableBytes() / (300 * 1024);
                }

                if (timeout > BaseConfig.DEFAULT_TIMEOUT) {

                    channelHandlerContext.pipeline().replace("AUTO_CLOSER",
                            "AUTO_CLOSER", new ReadTimeoutHandler(timeout));
                }

                if (message instanceof FullHttpResponse) {

                    if (keepAlive) {

                        ((FullHttpResponse) message).headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);

                        channelHandlerContext.channel().writeAndFlush(message);

                    } else {

                        channelHandlerContext.channel().writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
                    }

                } else {

                    return channelHandlerContext.channel().writeAndFlush(message);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static void close(String channelID) {

        ChannelHandlerContext channelHandlerContext = CHANNELS_MAP.get(channelID);

        if (channelHandlerContext != null) {

            //channelHandlerContext.flush().addListener(ChannelFutureListener.CLOSE);
            //channelHandlerContext.flush().;
            //channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

            channelHandlerContext.close();
        }
    }*/

    public static Map<String, Integer> getConnectionsCount() {

        int mainNewConnectionsCount = MainChannelDataProcessor.getNewConnectionsCount();
        int telnetNewConnectionsCount = TelnetChannelDataProcessor.getNewConnectionsCount();
        //int webNewConnectionsCount = HttpStaticFileServerHandler.getNewConnectionsCount();
        int totalConnectionsCount = mainNewConnectionsCount + telnetNewConnectionsCount;// + webNewConnectionsCount;
        int activeConnectionsCount = REQUESTS_EXECUTOR.getActiveCount();

        Map<String, Integer> connectionsCount = new HashMap<>();
        connectionsCount.put("mainPipe", mainNewConnectionsCount);
        connectionsCount.put("telnetPipe", telnetNewConnectionsCount);
        //connectionsCount.put("webPipe", webNewConnectionsCount);
        connectionsCount.put("total", totalConnectionsCount);
        connectionsCount.put("active", activeConnectionsCount);

        return connectionsCount;
    }
}