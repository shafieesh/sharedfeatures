/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.chainedminds.network.netty.telnetPipe;

import com.chainedminds._Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class TelnetPipeServer {

    private static final StringDecoder CHANNEL_DECODER = new StringDecoder();
    private static final StringEncoder CHANNEL_ENCODER = new StringEncoder();
    private static final TelnetChannelDataProcessor CHANNEL_DATA_PROCESSOR = new TelnetChannelDataProcessor();

    public static void start(EventLoopGroup connectionExecutor, EventLoopGroup ioExecutor) {

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(connectionExecutor, ioExecutor)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {

                            socketChannel.pipeline().addLast("AUTO_CLOSER", new ReadTimeoutHandler(_Config.DEFAULT_TIMEOUT));
                            socketChannel.pipeline().addLast("DELIMITER", new DelimiterBasedFrameDecoder(
                                    8192, Delimiters.lineDelimiter()));
                            socketChannel.pipeline().addLast("DECODER", CHANNEL_DECODER);
                            socketChannel.pipeline().addLast("PROCESSOR", CHANNEL_DATA_PROCESSOR);
                            socketChannel.pipeline().addLast("ENCODER", CHANNEL_ENCODER);
                        }
                    });

            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            //serverBootstrap.childOption(ChannelOption.SO_LINGER, 20);

            ChannelFuture channelFuture = serverBootstrap.bind(_Config.SERVER_PORT_TELNET).syncUninterruptibly();

            System.out.println("Starting telnet pipe server...");

            //channelFuture.channel().closeFuture().sync();

        } finally {

            //bossGroup.shutdownGracefully();
            //workerGroup.shutdownGracefully();
        }
    }
}
