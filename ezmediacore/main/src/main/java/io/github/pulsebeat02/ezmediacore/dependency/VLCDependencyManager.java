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
package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.EnhancedNativeDiscovery;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;


import java.io.IOException;
import java.nio.file.Path;

public final class VLCDependencyManager extends LibraryDependency {

  public VLCDependencyManager( final EzMediaCore core) throws IOException {
    super(core);
  }

  @Override
  public void start() throws IOException {
    final EnhancedNativeDiscovery discovery = new EnhancedNativeDiscovery();
    if (discovery.discover()) {
      final String result = discovery.getDiscoveredPath();
      this.onInstallation(Path.of(result));
      this.loadNativeLibVLC();
    }
  }

  @Override
  public void onInstallation( final Path path) {
    final EzMediaCore core = this.getCore();
    core.setVLCStatus(true);
    core.setVlcPath(path);
    core.getLogger().info(Locale.BINARY_PATHS.build("VLC", path));
  }

  private void loadNativeLibVLC() {
    if (this.isSupported()) {
      this.executePhantomPlayers();
    }
  }

  private boolean isSupported() {
    return this.getCore().isVLCSupported();
  }

  private void executePhantomPlayers() {
    new NativePluginLoader(this.getCore()).executePhantomPlayers();
  }
}
