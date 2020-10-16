package com.chainedminds.network.netty.mainPipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class MainChannelEncoder extends MessageToByteEncoder<byte[]> {

    @Override
    protected void encode(ChannelHandlerContext context, byte[] message, ByteBuf buffer) {

        byte[] dataLengthBytes = new byte[10];

        int messageLength = message.length;

        for (int i = dataLengthBytes.length - 1, j = 1; i >= 0; i--, j *= 10) {

            dataLengthBytes[i] = (byte) ((messageLength / j) % 10);
        }

        buffer.writeBytes(dataLengthBytes);

        buffer.writeBytes(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {

        //cause.printStackTrace();

        context.close();
    }
}