package io.github.pulsebeat02.ezmediacore.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLException;
import java.io.File;
import java.nio.file.Path;
import java.security.cert.CertificateException;

public final class JettyServer {

  private static final boolean SSL_ENABLED;

  static {
    SSL_ENABLED = System.getProperty("ssl") != null;
  }

  private final Path path;
  private final ServerBootstrap bootstrap;
  private final SslContext context;
  private final NioEventLoopGroup parent;
  private final NioEventLoopGroup children;
  private final int port;

  public JettyServer(@NotNull final Path path, final int port) {
    this.path = path;
    this.context = this.validateSslContext();
    this.parent = new NioEventLoopGroup();
    this.children = new NioEventLoopGroup();
    this.port = port;
    this.bootstrap = new ServerBootstrap();
    this.bootstrap.group(this.parent, this.children);
    this.bootstrap.channel(NioServerSocketChannel.class);
    this.bootstrap.handler(new LoggingHandler(LogLevel.INFO));
    this.bootstrap.childHandler(new FileChannelInitializer(path, this.context));
  }

  private @Nullable SslContext validateSslContext() {
    if (SSL_ENABLED) {
      try {
        final SelfSignedCertificate certificate = new SelfSignedCertificate();
        final File file = certificate.certificate();
        final File key = certificate.privateKey();
        return SslContextBuilder.forServer(file, key).build();
      } catch (final CertificateException | SSLException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  public void start() {
    try {
      final Channel channel = this.bootstrap.bind(this.port).sync().channel();
      channel.closeFuture().sync();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
