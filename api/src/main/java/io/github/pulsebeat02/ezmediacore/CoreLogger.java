package io.github.pulsebeat02.ezmediacore;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public interface CoreLogger extends AutoCloseable {

  void start() throws IOException;

  void info(@NotNull Object info);

  void warn(@NotNull Object warning);

  void error(@NotNull Object error);

  void vlc(@NotNull String line);

  void ffmpegPlayer(@NotNull String line);

  void ffmpegStream(@NotNull String line);

  void rtp(@NotNull String line);
}
