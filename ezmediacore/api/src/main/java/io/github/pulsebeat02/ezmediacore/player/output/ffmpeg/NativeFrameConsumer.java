package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NativeFrameConsumer {

  void consumeStreams(@NotNull final List<FFmpegBufferedStream> streams);
  void consume(@Nullable final FFmpegBufferedFrame frame);
}
