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
package io.github.pulsebeat02.ezmediacore.player;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FrameConfiguration {

  public static final FrameConfiguration FPS_10;
  public static final FrameConfiguration FPS_15;
  public static final FrameConfiguration FPS_20;
  public static final FrameConfiguration FPS_25;
  public static final FrameConfiguration FPS_30;
  public static final FrameConfiguration FPS_35;

  static {
    FPS_10 = ofFps(10);
    FPS_15 = ofFps(15);
    FPS_20 = ofFps(20);
    FPS_25 = ofFps(25);
    FPS_30 = ofFps(30);
    FPS_35 = ofFps(35);
  }

  private final int fps;

  FrameConfiguration(final int fps) {
    checkArgument(fps >= 0, "FPS must be greater than or equal to 0!");
    this.fps = fps;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull FrameConfiguration ofFps(final int fps) {
    return new FrameConfiguration(fps);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull FrameConfiguration ofFps(@NotNull final Number fps) {
    return ofFps((int) fps);
  }

  public int getFps() {
    return this.fps;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{fps=%s}".formatted(this.fps);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof FrameConfiguration)) {
      return false;
    }
    return ((FrameConfiguration) obj).fps == this.fps;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.fps);
  }
}
