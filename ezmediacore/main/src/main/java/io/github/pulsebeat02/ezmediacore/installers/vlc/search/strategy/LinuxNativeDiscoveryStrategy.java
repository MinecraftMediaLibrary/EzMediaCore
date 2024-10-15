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
package io.github.pulsebeat02.ezmediacore.installers.vlc.search.strategy;

import io.github.pulsebeat02.ezmediacore.installers.vlc.search.provider.DirectoryProviderDiscoveryStrategy;
import uk.co.caprica.vlcj.binding.lib.LibC;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;

public class LinuxNativeDiscoveryStrategy extends DirectoryProviderDiscoveryStrategy {

  private static final String[] FILENAME_PATTERNS = new String[]{
      "libvlc\\.so(?:\\.\\d)*",
      "libvlccore\\.so(?:\\.\\d)*"
  };

  private static final String[] PLUGIN_PATH_FORMATS = new String[]{
      "%s/plugins",
      "%s/vlc/plugins"
  };

  public LinuxNativeDiscoveryStrategy() {
    super(FILENAME_PATTERNS, PLUGIN_PATH_FORMATS);
  }

  @Override
  public boolean supported() {
    return RuntimeUtil.isNix();
  }

  @Override
  protected boolean setPluginPath(final String pluginPath) {
    return LibC.INSTANCE.setenv(BaseNativeDiscoveryStrategy.PLUGIN_ENV_NAME, pluginPath, 1) == 0;
  }
}
