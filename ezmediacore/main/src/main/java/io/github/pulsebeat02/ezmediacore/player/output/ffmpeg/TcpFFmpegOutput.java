package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.player.output.ServerOutput;
import io.github.pulsebeat02.ezmediacore.utility.misc.ConcatenationUtils;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TcpFFmpegOutput extends FFmpegOutputConfiguration {

  private ServerOutput output;

  TcpFFmpegOutput() {}

  @Contract(" -> new")
  public static @NotNull TcpFFmpegOutput ofOutput() {
    return new TcpFFmpegOutput();
  }

  @Override
  public @NotNull ServerOutput getResultingOutput() {
    return this.output;
  }

  @Override
  public void setOutput(@NotNull final Object output) {
    Preconditions.checkArgument(output instanceof ServerOutput);
    this.output = (ServerOutput) output;
  }

  @Override
  public @NotNull String toString() {
    final Map<String, String> configuration = this.getConfiguration();
    final String raw = this.output.getRaw();
    return ConcatenationUtils.mapOutputString(configuration, raw);
  }
}
