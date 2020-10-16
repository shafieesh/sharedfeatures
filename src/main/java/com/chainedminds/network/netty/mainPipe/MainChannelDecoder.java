package com.chainedminds.network.netty.mainPipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MainChannelDecoder extends ByteToMessageDecoder {

    private int state = 0;
    private int availableBytes = 10;

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> objects) {

        while (buffer.readableBytes() >= availableBytes) {

            if (state == 0) {

                if (buffer.readableBytes() >= availableBytes) {

                    availableBytes = 0;

                    for (int i = 10 - 1, j = 1000000000; i >= 0; i--, j /= 10) {

                        availableBytes += buffer.readByte() * j;
                    }

                    state = 1;
                }
            }

            if (state == 1) {

                if (buffer.readableBytes() >= availableBytes) {

                    byte[] sharedBuffer = new byte[availableBytes];

                    buffer.readBytes(sharedBuffer, 0, availableBytes);

                    objects.add(sharedBuffer);

                    availableBytes = 10;

                    state = 0;
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