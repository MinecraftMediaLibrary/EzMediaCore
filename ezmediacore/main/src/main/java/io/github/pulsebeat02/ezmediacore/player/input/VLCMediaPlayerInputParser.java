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
package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public final class VLCMediaPlayerInputParser extends MediaPlayerInputParser {

  public VLCMediaPlayerInputParser(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseUrl(@NotNull final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parsePath(@NotNull final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDevice(@NotNull final Input input) {
    throw new UnsupportedOperationException(
        "Device input device not supported for VLC Media Player!");
  }

  @Override
  public @NotNull Pair<Object, String[]> parseMrl(@NotNull final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDesktop(@NotNull final Input input) {
    // -I dummy screen://
    // Arguments: -I dummy
    // URL to Play: screen://
    return Pair.ofPair("screen://", new String[] {"-I", "dummy"});
  }

  @Override
  public @NotNull Pair<Object, String[]> parseWindow(@NotNull final Input input) {
    throw new UnsupportedOperationException(
        "Window input device not supported for VLC Media Player!");
  }
}
