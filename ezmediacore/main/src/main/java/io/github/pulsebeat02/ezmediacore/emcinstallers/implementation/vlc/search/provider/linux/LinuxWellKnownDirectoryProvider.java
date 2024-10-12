/**
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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.linux;

import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.WellKnownDirectoryProvider;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;

public class LinuxWellKnownDirectoryProvider extends WellKnownDirectoryProvider {

  private static final String[] DIRECTORIES;

  static {
    final String usr = "/usr";
    final String lib = String.format("%s/lib", usr);
    final String locallib = "/usr/local/lib";
    DIRECTORIES = new String[]{
        String.format("%s/x86_64-linux-gnu", lib),
        String.format("%s64", lib),
        String.format("%s/lib64", locallib),
        String.format("%s/i386-linux-gnu", lib),
        lib,
        locallib
    };
  }

  @Override
  public String[] directories() {
    return DIRECTORIES;
  }

  @Override
  public boolean supported() {
    return RuntimeUtil.isNix();
  }
}
