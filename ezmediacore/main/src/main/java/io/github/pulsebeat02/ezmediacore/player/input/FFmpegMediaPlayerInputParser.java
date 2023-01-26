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
package io.github.pulsebeat02.ezmediacore.player.input;

import com.github.kokorin.jaffree.ffmpeg.CaptureInput;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class FFmpegMediaPlayerInputParser extends MediaPlayerInputParser {

  private static final Constructor<?> CAPTURE_INPUT_CONSTRUCTOR;

  static {
    try {
      CAPTURE_INPUT_CONSTRUCTOR = CaptureInput.class.getDeclaredConstructor(String.class);
      CAPTURE_INPUT_CONSTRUCTOR.setAccessible(true);
    } catch (final NoSuchMethodException e) {
      throw new AssertionError("Could not create custom capture device!");
    }
  }

  public FFmpegMediaPlayerInputParser(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseUrl(@NotNull final Input input) {
    return Pair.ofPair(
        com.github.kokorin.jaffree.ffmpeg.UrlInput.fromUrl(input.getInput()), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parsePath(@NotNull final Input input) {
    return Pair.ofPair(
        com.github.kokorin.jaffree.ffmpeg.UrlInput.fromPath(Path.of(input.getInput())), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDevice(@NotNull final Input input) {
    if (this.isWindows()) {
      return Pair.ofPair(this.createCustomCapture(input.getInput()), EMPTY_ARGS);
    } else {
      throw new UnsupportedOperationException("Window input device only supported for Windows!");
    }
  }

  private @NotNull CaptureInput<?> createCustomCapture(@NotNull final String deviceName) {
    try {
      return (CaptureInput<?>) CAPTURE_INPUT_CONSTRUCTOR.newInstance(deviceName);
    } catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new AssertionError("Could not create custom capture device!");
    }
  }

  @Override
  public @NotNull Pair<Object, String[]> parseMrl(@NotNull final Input input) {
    return Pair.ofPair(
        com.github.kokorin.jaffree.ffmpeg.UrlInput.fromUrl(input.getInput()), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDesktop(@NotNull final Input input) {
    return Pair.ofPair(CaptureInput.captureDesktop(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseWindow(@NotNull final Input input) {
    if (this.isWindows()) {
      return Pair.ofPair(CaptureInput.WindowsGdiGrab.captureWindow(input.getInput()), EMPTY_ARGS);
    } else {
      throw new UnsupportedOperationException("Window input device only supported for Windows!");
    }
  }
}
