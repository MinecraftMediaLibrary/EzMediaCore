package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import io.github.pulsebeat02.ezmediacore.player.output.TcpOutput;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TcpFFmpegOutput extends FFmpegOutputConfiguration<TcpOutput> {

  private final TcpOutput output;

  TcpFFmpegOutput(@NotNull final TcpOutput output) {
    this.output = output;
  }

  @Contract("_ -> new")
  public static @NotNull TcpFFmpegOutput ofOutput(@NotNull final TcpOutput output) {
    return new TcpFFmpegOutput(output);
  }

  @Override
  public @NotNull TcpOutput getResultingOutput() {
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
    builder.append(this.output.getRaw());
    return builder.toString();
  }
}
