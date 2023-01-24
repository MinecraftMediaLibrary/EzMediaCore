package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.player.output.StreamOutput;
import io.github.pulsebeat02.ezmediacore.utility.misc.ConcatenationUtils;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StdoutFFmpegOutput extends FFmpegOutputConfiguration {

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
  public void setOutput(@NotNull final Object output) {
    Preconditions.checkArgument(output instanceof StreamOutput);
    this.output = (StreamOutput) output;
  }

  @Override
  public @NotNull String toString() {
    final Map<String, String> configuration = this.getConfiguration();
    final String raw = "pipe\\:1";
    return ConcatenationUtils.mapOutputString(configuration, raw);
  }
}
