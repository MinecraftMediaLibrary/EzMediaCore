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

import com.github.kokorin.jaffree.ffmpeg.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FFmpegMediaStream implements FFmpegBufferedStream {

  private final int id;
  private final long timebase;

  FFmpegMediaStream(final int id, final long timebase) {
    this.id = id;
    this.timebase = timebase;
  }

  @Contract("_ -> new")
  public static @NotNull FFmpegMediaStream ofStream(@NotNull final Stream stream) {
    return new FFmpegMediaStream(stream.getId(), stream.getTimebase());
  }

  @Contract("_, _ -> new")
  public static @NotNull FFmpegMediaStream ofStream(final int id, final long timebase) {
    return new FFmpegMediaStream(id, timebase);
  }


  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public long getTimebase() {
    return this.timebase;
  }
}
