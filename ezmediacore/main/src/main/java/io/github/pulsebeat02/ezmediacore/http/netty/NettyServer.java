/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.http.netty;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
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
import javax.net.ssl.SSLException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NettyServer implements HttpDaemon {

  private static final boolean SSL_ENABLED;
  private static final SslContext SSL_CONTEXT;
  private static final NioEventLoopGroup PARENT_GROUP;
  private static final NioEventLoopGroup CHILD_GROUP;

  static {
    SSL_ENABLED = System.getProperty("ssl") != null;
    SSL_CONTEXT = retrieveSslContext();
    PARENT_GROUP = new NioEventLoopGroup();
    CHILD_GROUP = new NioEventLoopGroup();
  }

  @Nullable
  private static SslContext retrieveSslContext() {
    SslContext temporary = null;
    if (SSL_ENABLED) {
      try {
        final SelfSignedCertificate certificate = new SelfSignedCertificate();
        final File file = certificate.certificate();
        final File key = certificate.privateKey();
        temporary = SslContextBuilder.forServer(file, key).build();
      } catch (final CertificateException | SSLException ignored) {
      }
    }
    return temporary;
  }

  private final MediaLibraryCore core;
  private final Path path;
  private final String ip;
  private final int port;
  private final boolean verbose;

  private final ServerBootstrap bootstrap;
  private Channel channel;

  public NettyServer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    this.core = core;
    this.path = path;
    this.ip = ip;
    this.port = port;
    this.verbose = verbose;
    this.bootstrap = new ServerBootstrap();
    this.bootstrap.channel(NioServerSocketChannel.class);
    this.bootstrap.group(PARENT_GROUP, CHILD_GROUP);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull NettyServer ofDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    return ofDaemon(core, core.getHttpServerPath(), ip, port, verbose);
  }

  @Contract("_, _, _, _, _ -> new")
  public static @NotNull NettyServer ofDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    return new NettyServer(core, path, ip, port, verbose);
  }

  @Override
  public void start() {
    try {
      this.createBootstrap();
      this.channel = this.bootstrap.bind(this.port).sync().channel();
      this.channel.closeFuture().sync();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void createBootstrap() {
    final LogLevel level = this.verbose ? LogLevel.INFO : LogLevel.ERROR;
    final LoggingHandler handler = new LoggingHandler(level);
    final FileChannelInitializer initializer =
        new FileChannelInitializer(this.path, SSL_CONTEXT, this.ip, this.port);
    this.bootstrap.handler(handler);
    this.bootstrap.childHandler(initializer);
  }

  @Override
  public void onServerStart() {}

  @Override
  public void stop() {
    this.onServerTermination();
    if (this.channel != null) {
      this.closeChannels();
    }
  }

  private void closeChannels() {
    try {
      this.channel.close().sync();
      this.channel.parent().close().sync();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onServerTermination() {}

  @Override
  public boolean isVerbose() {
    return false;
  }

  @Override
  public @NotNull Path getServerPath() {
    return this.path;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public @NotNull String getAddress() {
    return this.ip;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
