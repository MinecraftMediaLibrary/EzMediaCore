package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import io.github.pulsebeat02.ezmediacore.player.output.StreamOutput;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StdoutFFmpegOutput extends FFmpegOutputConfiguration<StreamOutput> {

  private final StreamOutput output;

  StdoutFFmpegOutput(@NotNull final StreamOutput output) {
    this.output = output;
  }

  @Contract("_ -> new")
  public static @NotNull StdoutFFmpegOutput ofOutput(@NotNull final StreamOutput output) {
    return new StdoutFFmpegOutput(output);
  }

  @Override
  public @NotNull StreamOutput getResultingOutput() {
    return this.output;
  }

  @Override
  public @NotNull String toString() {
    final Map<String, String> configuration = this.getConfiguration();
    final StringBuilder builder = new StringBuilder("[");
    for (final Map.Entry<String, String> entry : configuration.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(":");
    }
    builder.replace(builder.length() - 1, builder.length(), "]");
    builder.append("pipe\\:1");
    return builder.toString();
  }
}
