package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import io.github.pulsebeat02.ezmediacore.player.output.TcpOutput;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TcpFFmpegOutput extends FFmpegOutputConfiguration<TcpOutput> {

  private TcpOutput output;

  TcpFFmpegOutput() {}

  @Contract(" -> new")
  public static @NotNull TcpFFmpegOutput ofOutput() {
    return new TcpFFmpegOutput();
  }

  @Override
  public @NotNull TcpOutput getResultingOutput() {
    return this.output;
  }

  @Override
  public void setOutput(@NotNull final TcpOutput output) {
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
    builder.append(this.output.getRaw());
    return builder.toString();
  }
}
