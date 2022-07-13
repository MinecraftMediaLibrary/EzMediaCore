package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import org.jetbrains.annotations.NotNull;

public final class FFmpegPlayerOutput implements FFmpegOutput<Void> {

  private final TcpFFmpegOutput tcp;
  private final StdoutFFmpegOutput stdout;

  FFmpegPlayerOutput() {
    this.tcp = new TcpFFmpegOutput();
    this.stdout = new StdoutFFmpegOutput();
  }

  public static @NotNull FFmpegPlayerOutput of() {
    return new FFmpegPlayerOutput();
  }

  public @NotNull TcpFFmpegOutput getTcp() {
    return this.tcp;
  }

  public @NotNull StdoutFFmpegOutput getStdout() {
    return this.stdout;
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
  public void setOutput(@NotNull final Void output) {}

  @Override
  public String toString() {
    return "-c:v libx264 -c:a aac -f tee -map 0 \"%s|%s\"".formatted(this.tcp, this.stdout);
  }
}
