package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import org.jetbrains.annotations.NotNull;

public final class FFmpegPlayerOutput implements FFmpegOutput<Void> {

  private TcpFFmpegOutput tcp;
  private StdoutFFmpegOutput stdout;

  FFmpegPlayerOutput() {}

  public static @NotNull FFmpegPlayerOutput of() {
    return new FFmpegPlayerOutput();
  }

  public void setTcpOutput(@NotNull final TcpFFmpegOutput output) {
    this.tcp = output;
  }

  public void setStdoutOutput(@NotNull final StdoutFFmpegOutput output) {
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

  @Override
  public void setOutput(@NotNull final Void output) {
  }

  @Override
  public String toString() {
    return "-c:v libx264 -c:a aac -f tee -map 0 \"%s|%s\"".formatted(this.tcp, this.stdout);
  }
}
