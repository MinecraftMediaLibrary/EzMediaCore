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

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import rewrite.util.tuples.Pair;


public final class VLCMediaPlayerInputParser extends MediaPlayerInputParser {

  public VLCMediaPlayerInputParser( final EzMediaCore core) {
    super(core);
  }

  @Override
  public  Pair<Object, String[]> parseUrl( final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public  Pair<Object, String[]> parsePath( final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public  Pair<Object, String[]> parseDevice( final Input input) {
    throw new UnsupportedOperationException(
        "Device input device not supported for VLC Media Player!");
  }

  @Override
  public  Pair<Object, String[]> parseMrl( final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public  Pair<Object, String[]> parseDesktop( final Input input) {
    // -I dummy screen://
    // Arguments: -I dummy
    // URL to Play: screen://
    return Pair.ofPair("screen://", new String[] {"-I", "dummy"});
  }

  @Override
  public  Pair<Object, String[]> parseWindow( final Input input) {
    throw new UnsupportedOperationException(
        "Window input device not supported for VLC Media Player!");
  }
}
