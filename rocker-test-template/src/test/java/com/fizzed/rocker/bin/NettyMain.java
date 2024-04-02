/*
 * Copyright 2015 Fizzed Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.rocker.bin;

import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocker8.Stocks;

public class NettyMain {
    static private final Logger log = LoggerFactory.getLogger(NettyMain.class);

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
    static final List<Stock> stocks = Stock.dummyItems();

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new HttpHelloWorldServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    public static class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;

        public HttpHelloWorldServerInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }
            
            p.addLast(new HttpResponseEncoder());
            
            p.addLast("httpDecoder", new HttpRequestDecoder());
            // Don't want to handle HttpChunks
            p.addLast("httpAggregator", new HttpObjectAggregator(1048576));

            p.addLast(new HttpHelloWorldServerHandler());
        }
    }

    public static class HttpHelloWorldServerHandler extends SimpleChannelInboundHandler<Object> {

        private static final byte[] CONTENT = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};

        boolean keepAlive;

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            //log.info("netty msg: {}", msg);

            if (msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest)msg;
                    
                RockerOutput out = rocker8.Stocks.template(stocks)
                    .render();

                //
                // we can get quite efficient depending on output selected
                //
                ByteBuf buffer;

                // wrap the underlying arrays!
                if (out instanceof ArrayOfByteArraysOutput) {
                    ArrayOfByteArraysOutput abaos = (ArrayOfByteArraysOutput)out;
                    List<byte[]> arrays = abaos.getArrays();
                    byte[][] arrayOfArrays = arrays.toArray(new byte[0][]);
                    buffer = Unpooled.wrappedBuffer(arrayOfArrays);
                }
                else {
                    // save to assume output is just a string w/ charset
                    buffer = Unpooled.copiedBuffer(out.toString(), out.getCharsetName());
                }

                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
                response.headers().set(CONTENT_TYPE, "text/html; charset=" + out.getCharsetName());
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                if (!keepAlive) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, Values.KEEP_ALIVE);
                    ctx.write(response);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}