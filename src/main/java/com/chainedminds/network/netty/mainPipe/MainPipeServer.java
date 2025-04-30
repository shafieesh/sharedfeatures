package com.chainedminds.network.netty.mainPipe;

import com.chainedminds._Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class MainPipeServer {

    public static void start(EventLoopGroup connectionExecutor, EventLoopGroup ioExecutor, boolean keepAlive) {

        try {

            final MainChannelProcessor processor = new MainChannelProcessor();
            final MainChannelEncoder encoder = new MainChannelEncoder();
            final MainChannelDecoder decoder = new MainChannelDecoder();

            MainChannelProcessor.KEEP_ALIVE = keepAlive;

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(connectionExecutor, ioExecutor)
                    .channel(NioServerSocketChannel.class)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {

                            socketChannel.pipeline().addLast("AUTO_CLOSER", new ReadTimeoutHandler(_Config.DEFAULT_TIMEOUT));
                            socketChannel.pipeline().addLast("DECODER", decoder);
                            socketChannel.pipeline().addLast("PROCESSOR", processor);
                            socketChannel.pipeline().addLast("ENCODER", encoder);
                        }
                    });

            //serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            //serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            //serverBootstrap.childOption(ChannelOption.SO_LINGER, 20);

            ChannelFuture channelFuture = serverBootstrap.bind(_Config.SERVER_PORT_MAIN).syncUninterruptibly();

            System.out.println("Starting main pipe server...");

            //channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            //workerGroup.shutdownGracefully();

            //bossGroup.shutdownGracefully();
        }
    }

    /*public static void writeMessage(String id, byte[] message) {

        CHANNEL_GROUP.writeAndFlush(message, channel -> channel.id().asLongText().equals(id));

        //CHANNEL_GROUP.writeAndFlush(message);
    }*/
}