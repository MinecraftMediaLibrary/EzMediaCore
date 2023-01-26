package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.player.output.ServerOutput;
import io.github.pulsebeat02.ezmediacore.utility.misc.ConcatenationUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class RTSPFFmpegOutput extends FFmpegOutputConfiguration {

  private ServerOutput output;

  RTSPFFmpegOutput() {}

  @Contract(" -> new")
  public static @NotNull RTSPFFmpegOutput ofOutput() {
    return new RTSPFFmpegOutput();
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
