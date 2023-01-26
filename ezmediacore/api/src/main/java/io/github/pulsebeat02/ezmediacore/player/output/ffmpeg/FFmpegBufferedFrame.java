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

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FFmpegBufferedFrame {

  private final int streamId;
  private final long pts;
  private final int[] image;
  private final BufferCarrier samples;

  FFmpegBufferedFrame(final int streamId, final long pts, final int @NotNull [] image, @NotNull final BufferCarrier samples) {
    this.streamId = streamId;
    this.pts = pts;
    this.image = image;
    this.samples = samples;
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegBufferedFrame ofFrame(final int streamId, final long pts, final int @NotNull [] image, @NotNull final BufferCarrier samples) {
    return new FFmpegBufferedFrame(streamId, pts, image, samples);
  }

  public int getStreamId() {
    return this.streamId;
  }

  public long getPts() {
    return this.pts;
  }

  public int @NotNull [] getImage() {
    return this.image;
  }

  public @NotNull BufferCarrier getSamples() {
    return this.samples;
  }
}
