/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import org.jetbrains.annotations.NotNull;

public final class FFmpegPlayerOutput implements FFmpegOutput {

  private final TcpFFmpegOutput tcp;
  private final FFmpegOutputConfiguration varied; // doesn't have to be stdout

  FFmpegPlayerOutput(@NotNull final TcpFFmpegOutput tcp, @NotNull final FFmpegOutputConfiguration varied) {
    this.tcp = tcp;
    this.varied = varied;
  }

  FFmpegPlayerOutput() {
    this(new TcpFFmpegOutput(), new StdoutFFmpegOutput());
  }

  public static @NotNull FFmpegPlayerOutput of() {
    return new FFmpegPlayerOutput();
  }

  public static @NotNull FFmpegPlayerOutput of(
      @NotNull final TcpFFmpegOutput tcp, @NotNull final FFmpegOutputConfiguration varied) {
    return new FFmpegPlayerOutput(tcp, varied);
  }

  public @NotNull TcpFFmpegOutput getTcp() {
    return this.tcp;
  }

  public @NotNull FFmpegOutputConfiguration getVariedOutput() {
    return this.varied;
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
  public void setOutput(@NotNull final Object output) {}

  @Override
  public String toString() {
    return "-c:v libx264 -c:a aac -f tee -map 0 \"%s|%s\"".formatted(this.tcp, this.varied);
  }
}
