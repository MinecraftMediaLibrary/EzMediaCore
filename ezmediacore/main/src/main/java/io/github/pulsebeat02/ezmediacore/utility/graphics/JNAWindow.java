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
package io.github.pulsebeat02.ezmediacore.utility.graphics;

import com.sun.jna.platform.DesktopWindow;
import io.github.pulsebeat02.ezmediacore.player.input.Window;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

public final class JNAWindow implements Window {

  private final String path;
  private final String title;
  private final int width;
  private final int height;
  private final int x;
  private final int y;

  JNAWindow(@NotNull final DesktopWindow window) {
    final Rectangle rectangle = window.getLocAndSize();
    this.path = window.getFilePath();
    this.title = window.getTitle();
    this.width = (int) rectangle.getWidth();
    this.height = (int) rectangle.getHeight();
    this.x = (int) rectangle.getX();
    this.y = (int) rectangle.getY();
  }

  @Override
  public @NotNull String getFilePath() {
    return this.path;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getX() {
    return this.x;
  }

  @Override
  public int getY() {
    return this.y;
  }

  @Override
  public @NotNull String getTitle() {
    return this.title;
  }
}
