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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.win;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

import com.sun.jna.platform.win32.Advapi32Util;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.DiscoveryProviderPriority;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;

public class WindowsInstallDirectoryProvider implements DiscoveryDirectoryProvider {

  private static final String VLC_REGISTRY_KEY;
  private static final String VLC_INSTALL_DIR_KEY;

  static {
    VLC_REGISTRY_KEY = "SOFTWARE\\VideoLAN\\VLC";
    VLC_INSTALL_DIR_KEY = "InstallDir";
  }

  @Override
  public int priority() {
    return DiscoveryProviderPriority.INSTALL_DIR;
  }

  @Override
  public String[] directories() {
    final String installDir = this.getVlcInstallDir();
    return installDir != null ? new String[]{installDir} : new String[0];
  }

  @Override
  public boolean supported() {
    return RuntimeUtil.isWindows();
  }

  private String getVlcInstallDir() {
    try {
      return Advapi32Util.registryGetStringValue(HKEY_LOCAL_MACHINE, VLC_REGISTRY_KEY,
          VLC_INSTALL_DIR_KEY);
    } catch (final Exception e) {
      return null;
    }
  }
}
