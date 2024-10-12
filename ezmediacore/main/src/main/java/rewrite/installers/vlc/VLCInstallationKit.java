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
package rewrite.installers.vlc;

import rewrite.installers.BaseInstaller;
import rewrite.installers.vlc.installation.VLCInstaller;
import rewrite.installers.vlc.search.EnhancedNativeDiscovery;
import rewrite.installers.vlc.search.provider.DirectoryProviderDiscoveryStrategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/** VLC Installation manager. */
public final class VLCInstallationKit {

  private final Path[] others;

  VLCInstallationKit(final Path... others) {
    this.others = others;
  }

  VLCInstallationKit() {
    this(BaseInstaller.getDefaultExecutableFolderPath());
  }

  /**
   * Attempts to search for the VLC binary in common installation paths. If a library has been found
   * and loaded successfully, it will be available to be used by VLCJ. Otherwise, if a library could
   * not be found, it will download the respective binary for the user operating system and load
   * that libvlc that way.
   *
   * @return Optional containing path if found, otherwise empty
   * @throws IOException if an issue occurred during installation
   */
  public Optional<Path> start() throws IOException {
    return this.installBinary(true, this.others);
  }

  private Optional<Path> installBinary(final boolean chmod, final Path... others)
      throws IOException {
    this.addExtraSearchPaths(others);
    final Optional<Path> binary = this.searchAndLoadBinary();
    if (binary.isPresent()) {
      return binary;
    }
    return this.loadInstalledBinaries(chmod);
  }

  private Optional<Path> loadInstalledBinaries(final boolean chmod) throws IOException {
    this.addDownloadedSearchPath(chmod);
    return this.searchAndLoadBinary();
  }

  private void addDownloadedSearchPath(final boolean chmod) throws IOException {
    final VLCInstaller installer = VLCInstaller.create();
    final Path download = installer.download(chmod);
    DirectoryProviderDiscoveryStrategy.addSearchDirectory(download);
  }

  private void addExtraSearchPaths(final Path[] others) {
    Arrays.stream(others).forEach(DirectoryProviderDiscoveryStrategy::addSearchDirectory);
  }

  private Optional<Path> searchAndLoadBinary() {
    final EnhancedNativeDiscovery discovery = new EnhancedNativeDiscovery();
    if (discovery.discover()) {
      final String discovered = discovery.getDiscoveredPath();
      final Path path = Path.of(discovered);
      return Optional.of(path);
    }
    return Optional.empty();
  }

  /**
   * Constructs a new VLCInstallationKit with default parameters for downloading/loading libvlc into
   * the runtime.
   *
   * @return a new VLCInstallationKit
   */
  public static VLCInstallationKit create() {
    return new VLCInstallationKit();
  }

  /**
   * Constructs a new VLCInstallationKit with the specified path(s) for downloading/loading libvlc
   * into the runtime.
   *
   * @param others the desired paths to search
   * @return a new VLCInstallationKit
   */
  public static VLCInstallationKit create(final Path... others) {
    return new VLCInstallationKit(others);
  }
}
