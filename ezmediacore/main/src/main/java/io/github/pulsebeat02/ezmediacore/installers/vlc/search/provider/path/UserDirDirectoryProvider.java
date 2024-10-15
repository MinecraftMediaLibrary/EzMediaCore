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
package io.github.pulsebeat02.ezmediacore.installers.vlc.search.provider.path;

import io.github.pulsebeat02.ezmediacore.installers.vlc.search.provider.DiscoveryDirectoryProvider;
import io.github.pulsebeat02.ezmediacore.installers.vlc.search.provider.DiscoveryProviderPriority;

public class UserDirDirectoryProvider implements DiscoveryDirectoryProvider {

  private static final String USER_DIR = System.getProperty("user.dir");

  @Override
  public int priority() {
    return DiscoveryProviderPriority.USER_DIR;
  }

  @Override
  public String[] directories() {
    return USER_DIR != null ? new String[]{USER_DIR} : new String[0];
  }

  @Override
  public boolean supported() {
    return true;
  }

}
