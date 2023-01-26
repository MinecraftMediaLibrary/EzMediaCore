package io.github.pulsebeat02.ezmediacore;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public interface CoreLogger extends AutoCloseable {

  void start() throws IOException;

  void info(@NotNull final Object info);

  void warn(@NotNull final Object warning);

  void error(@NotNull final Object error);

  void vlc(@NotNull final String line);

  void ffmpegPlayer(@NotNull final String line);

  void ffmpegStream(@NotNull final String line);

  void rtp(@NotNull final String line);
}
