package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import io.github.pulsebeat02.ezmediacore.player.output.ConfiguredOutput;
import org.jetbrains.annotations.NotNull;

public final class FFmpegPlayerOutput implements FFmpegOutput<Void> {

  private ConfiguredOutput tcp;
  private ConfiguredOutput stdout;

  FFmpegPlayerOutput() {
  }

  public static @NotNull FFmpegPlayerOutput of() {
    return new FFmpegPlayerOutput();
  }


  public void setTcpOutput(@NotNull final ConfiguredOutput output) {
    this.tcp = output;
  }

  public void setStdoutOutput(@NotNull final ConfiguredOutput output) {
    this.stdout = output;
  }


  /*
  No physical output as the ConfiguredOutput (TcpFFmpegOutput and StdoutFFmpegOutput) already
  have their respective output methods.
   */
  @Override
  public @NotNull Void getResultingOutput() {
    return null;
  }
}
