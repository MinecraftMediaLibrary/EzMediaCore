/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.capability.installer.vlc;

import java.nio.file.Path;
import java.util.Optional;
import me.brandonli.mcav.capability.installer.FileMode;
import org.junit.jupiter.api.Assumptions;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

// The VLC that VlcInstaller downloads into the cache of the library, never the one installed on this machine, so the VLC
// tests behave alike on every host. The download happens once per machine and is reused after that.
public final class DownloadedVlc {

  private DownloadedVlc() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated!");
  }

  // Loads the downloaded VLC through the installation kit, downloading it first when the cache has none.
  public static Optional<Path> load() {
    final VlcInstaller installer = VlcInstaller.create();
    final NativeVlcDiscovery discovery = createDiscovery();
    final VlcLoadState loadState = new VlcLoadState();
    final VlcInstallationKit kit = new VlcInstallationKit(installer, discovery, loadState);
    return kit.start();
  }

  // Returns the library directory of the downloaded VLC, and skips the test on a platform VLC has no download for.
  static Path install() {
    final VlcInstaller installer = VlcInstaller.create();
    final boolean supported = installer.isSupported();
    Assumptions.assumeTrue(supported, "VLC has no download for this platform!");

    final Optional<Path> existing = installer.findInstallation();
    return existing.orElseGet(() -> installer.download(FileMode.EXECUTABLE));
  }

  // A discovery that finds no VLC on the machine, so libvlc loads only from an installation directory.
  static NativeVlcDiscovery createDiscovery() {
    return new NativeVlcDiscovery(DownloadedVlc::createNoStrategies);
  }

  // vlcj falls back to its default strategies when it is given none, so a strategy that never applies stands in.
  private static NativeDiscoveryStrategy[] createNoStrategies() {
    final NativeDiscoveryStrategy unsupported = new UnsupportedStrategy();
    return new NativeDiscoveryStrategy[] { unsupported };
  }

  // vlcj asks a strategy for nothing else once it reports itself unsupported.
  private static final class UnsupportedStrategy implements NativeDiscoveryStrategy {

    private static final String UNREACHABLE_MESSAGE = "vlcj only asks a supported strategy!";

    @Override
    public boolean supported() {
      return false;
    }

    @Override
    public String discover() {
      throw new UnsupportedOperationException(UNREACHABLE_MESSAGE);
    }

    @Override
    public boolean onFound(final String path) {
      throw new UnsupportedOperationException(UNREACHABLE_MESSAGE);
    }

    @Override
    public boolean onSetPluginPath(final String path) {
      throw new UnsupportedOperationException(UNREACHABLE_MESSAGE);
    }
  }
}
