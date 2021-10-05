package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.config.ServerInfo;
import io.github.pulsebeat02.deluxemediaplugin.executors.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegMediaStreamer;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public abstract class FFmpegOutput extends AudioOutput implements FFmpegOutputHandle {

  public FFmpegOutput(@NotNull final String name) {
    super(name);
  }

  @Override
  public void setAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final Audience audience,
      @NotNull final String mrl) {}

  @NotNull
  @Override
  public String openFFmpegStream(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final String mrl) {
    final ServerInfo info = plugin.getHttpAudioServer();
    final String ip = info.getIp();
    final int port = info.getPort();
    final FFmpegMediaStreamer streamer =
        new FFmpegMediaStreamer(
            plugin.library(),
            plugin.getAudioConfiguration(),
            RequestUtils.getAudioURLs(mrl).get(0),
            ip,
            port);
    plugin.getAttributes().setStreamExtractor(streamer);
    streamer.executeAsync(ExecutorProvider.STREAM_THREAD_EXECUTOR);
    return "http://%s:%s/live.stream".formatted(ip, port);
  }
}
