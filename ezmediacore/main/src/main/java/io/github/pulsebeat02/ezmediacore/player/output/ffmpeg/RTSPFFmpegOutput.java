/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
