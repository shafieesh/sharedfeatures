package com.chainedminds.network.netty.mainPipe;

import com.chainedminds._Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class MainPipeServer {

    //TODO CHANGE TIMEOUT FOR GAMING

    private static final MainChannelDataProcessor CHANNEL_DATA_PROCESSOR = new MainChannelDataProcessor();
    private static final MainChannelEncoder CHANNEL_ENCODER = new MainChannelEncoder();

    public static void start(EventLoopGroup connectionExecutor, EventLoopGroup ioExecutor) {

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(connectionExecutor, ioExecutor)
                    .channel(NioServerSocketChannel.class)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {

                            socketChannel.pipeline().addLast("AUTO_CLOSER", new ReadTimeoutHandler(_Config.DEFAULT_TIMEOUT));
                            socketChannel.pipeline().addLast("DECODER", new MainChannelDecoder());
                            socketChannel.pipeline().addLast("PROCESSOR", CHANNEL_DATA_PROCESSOR);
                            socketChannel.pipeline().addLast("ENCODER", CHANNEL_ENCODER);
                        }
                    });

            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
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