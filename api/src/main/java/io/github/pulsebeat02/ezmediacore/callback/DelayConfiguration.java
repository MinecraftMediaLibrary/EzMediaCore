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

package io.github.pulsebeat02.ezmediacore.callback;

import org.jetbrains.annotations.NotNull;

public final class DelayConfiguration {

  public static final DelayConfiguration DELAY_0_MS;
  public static final DelayConfiguration DELAY_5_MS;
  public static final DelayConfiguration DELAY_10_MS;
  public static final DelayConfiguration DELAY_15_MS;
  public static final DelayConfiguration DELAY_20_MS;

  static {
    DELAY_0_MS = ofDelay(0);
    DELAY_5_MS = ofDelay(5);
    DELAY_10_MS = ofDelay(10);
    DELAY_15_MS = ofDelay(15);
    DELAY_20_MS = ofDelay(20);
  }

  private final int delay;

  DelayConfiguration(final int delay) {
    this.delay = delay;
  }

  public static @NotNull DelayConfiguration ofDelay(final int delay) {
    return new DelayConfiguration(delay);
  }

  public static @NotNull DelayConfiguration ofDelay(@NotNull final Number delay) {
    return ofDelay((int) delay);
  }

  public int getDelay() {
    return this.delay;
  }
}
