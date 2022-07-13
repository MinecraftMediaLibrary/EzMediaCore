package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import io.github.pulsebeat02.ezmediacore.player.output.StreamOutput;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StdoutFFmpegOutput extends FFmpegOutputConfiguration<StreamOutput> {

  private StreamOutput output;

  StdoutFFmpegOutput() {}

  @Contract(" -> new")
  public static @NotNull StdoutFFmpegOutput ofOutput() {
    return new StdoutFFmpegOutput();
  }

  @Override
  public @NotNull StreamOutput getResultingOutput() {
    return this.output;
  }

  @Override
  public void setOutput(@NotNull final StreamOutput output) {
    this.output = output;
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
