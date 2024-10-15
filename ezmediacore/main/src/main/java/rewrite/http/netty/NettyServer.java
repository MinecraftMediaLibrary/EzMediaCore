/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rewrite.http.netty;

import rewrite.EzMediaCore;
import io.netty.channel.ChannelFuture;
import rewrite.http.HttpDaemon;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.io.File;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLException;

public final class NettyServer implements HttpDaemon {

  private static final boolean SSL_ENABLED = System.getProperty("ssl") != null;
  private static final NioEventLoopGroup PARENT_GROUP = new NioEventLoopGroup();
  private static final NioEventLoopGroup CHILD_GROUP = new NioEventLoopGroup();
  private static SslContext SSL_CONTEXT;

  static {
    if (SSL_ENABLED) {
      try {
        final SelfSignedCertificate certificate = new SelfSignedCertificate();
        final File file = certificate.certificate();
        final File key = certificate.privateKey();
        SSL_CONTEXT = SslContextBuilder.forServer(file, key).build();
      } catch (final CertificateException | SSLException ignored) {
      }
    }
  }


  private final EzMediaCore core;
  private final Path path;
  private final String ip;
  private final int port;

  private final ServerBootstrap bootstrap;
  private Channel channel;

  public NettyServer(
       final EzMediaCore core,
       final Path path,
       final String ip,
      final int port) {
    this.core = core;
    this.path = path;
    this.ip = ip;
    this.port = port;
    this.bootstrap = new ServerBootstrap();
    this.bootstrap.channel(NioServerSocketChannel.class);
    this.bootstrap.group(PARENT_GROUP, CHILD_GROUP);
  }

  public static  NettyServer ofDaemon(
       final EzMediaCore core,
       final String ip,
      final int port) {
    return ofDaemon(core, core.getHttpServerPath(), ip, port);
  }

  public static  NettyServer ofDaemon(
       final EzMediaCore core,
       final Path path,
       final String ip,
      final int port) {
    return new NettyServer(core, path, ip, port);
  }

  @Override
  public void start() {
    try {
      this.createBootstrap();
      this.syncChannel();
      this.syncCloseChannel();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void syncCloseChannel() throws InterruptedException {
    final ChannelFuture closeFuture = this.channel.closeFuture();
    closeFuture.sync();
  }

  private void syncChannel() throws InterruptedException {
    final ChannelFuture future = this.bootstrap.bind();
    final ChannelFuture sync = future.sync();
    this.channel = sync.channel();
  }

  private void createBootstrap() {
    final LoggingHandler handler = new LoggingHandler(LogLevel.ERROR);
    final FileChannelInitializer initializer =
        new FileChannelInitializer(this.path, SSL_CONTEXT, this.ip);
    this.bootstrap.handler(handler);
    this.bootstrap.childHandler(initializer);
  }

  @Override
  public void stop() {
    if (this.channel != null) {
      this.closeChannels();
    }
  }

  private void closeChannels() {
    try {
      final ChannelFuture close = this.channel.close();
      final Channel parent = this.channel.parent();
      final ChannelFuture parentClose = parent.close();
      close.get(5, TimeUnit.SECONDS);
      parentClose.get(5, TimeUnit.SECONDS);
    } catch (final InterruptedException | ExecutionException | TimeoutException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public  Path getServerPath() {
    return this.path;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public  String getAddress() {
    return this.ip;
  }

  public  EzMediaCore getCore() {
    return this.core;
  }

  @Override
  public Path getRelativePath(final Path file) {
      final Path server = this.getServerPath();
      return server.relativize(file);
  }
}
