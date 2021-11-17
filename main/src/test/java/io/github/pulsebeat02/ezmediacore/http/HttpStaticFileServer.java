//package io.github.pulsebeat02.ezmediacore.http;
//
///*
// * Copyright 2012 The Netty Project
// *
// * The Netty Project licenses this file to you under the Apache License,
// * version 2.0 (the "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at:
// *
// *   https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations
// * under the License.
// */
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.SslProvider;
//import io.netty.handler.ssl.util.SelfSignedCertificate;
//
//public final class HttpStaticFileServer {
//
//  private static final int PORT = 8080;
//
//  public static void main(final String[] args) throws Exception {
//    final SelfSignedCertificate certificate = new SelfSignedCertificate();
//    final SslContext context =
//        SslContextBuilder.forServer(certificate.certificate(), certificate.privateKey())
//            .sslProvider(SslProvider.JDK)
//            .build();
//    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//    final EventLoopGroup workerGroup = new NioEventLoopGroup();
//    try {
//      final ServerBootstrap server = new ServerBootstrap();
//      server
//          .group(bossGroup, workerGroup)
//          .channel(NioServerSocketChannel.class)
//          .handler(new LoggingHandler(LogLevel.INFO))
//          .childHandler(new HttpStaticFileServerInitializer(context));
//      final Channel ch = server.bind(PORT).sync().channel();
//      System.out.printf("Open your web browser and navigate to https://127.0.0.1:%d/%n", PORT);
//      ch.closeFuture().sync();
//    } finally {
//      bossGroup.shutdownGracefully();
//      workerGroup.shutdownGracefully();
//    }
//  }
//}
