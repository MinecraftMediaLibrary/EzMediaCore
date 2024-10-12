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
import io.github.pulsebeat02.ezmediacore.utility.misc.OSType;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;
import io.github.pulsebeat02.ezmediacore.utility.structure.Pair;


public abstract class MediaPlayerInputParser implements InputParser {

  protected static final String[] EMPTY_ARGS;

  static {
    EMPTY_ARGS = new String[]{};
  }

  private final EzMediaCore core;
  private final boolean windows;

  public MediaPlayerInputParser( final EzMediaCore core) {
    this.core = core;
    this.windows = OSType.getCurrentOS() == OSType.WINDOWS;
  }

  @Override
  public  Pair<Object, String[]> parseInput( final Input input) {
    if (input instanceof final UrlInput urlInput) {
      return this.parseUrl(urlInput);
    } else if (input instanceof final PathInput pathInput) {
      return this.parsePath(pathInput);
    } else if (input instanceof final DeviceInput deviceInput) {
      return this.parseDevice(deviceInput);
    } else if (input instanceof final MrlInput mrlInput) {
      return this.parseMrl(mrlInput);
    } else if (input instanceof final DesktopInput desktopInput) {
      return this.parseDesktop(desktopInput);
    } else if (input instanceof final WindowInput windowInput) {
      return this.parseWindow(windowInput);
    } else {
      return Pair.ofPair(input.getInput(), new String[]{});
    }
  }

  @Override
  public  EzMediaCore getCore() {
    return this.core;
  }

  public boolean isWindows() {
    return this.windows;
  }
}
