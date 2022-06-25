package io.github.pulsebeat02.ezmediacore.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public final class FileChannelInitializer extends ChannelInitializer<Channel> {

  private final Path directory;
  private final SslContext context;
  private final boolean sslEnabled;

  public FileChannelInitializer(
      @NotNull final Path directory, @Nullable final SslContext context) {
    this.directory = directory;
    this.context = context;
    this.sslEnabled = context != null;
  }

  @Override
  protected void initChannel(@NotNull final Channel ch) {

    final ChannelPipeline pipeline = ch.pipeline();
    if (this.sslEnabled) {
      pipeline.addLast(this.context.newHandler(ch.alloc()));
    }

    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpObjectAggregator(65536));
    pipeline.addLast(new ChunkedWriteHandler());
    pipeline.addLast(new HttpServletHandler(this.directory));
  }
}
