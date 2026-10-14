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
import org.checkerframework.checker.nullness.qual.Nullable;

// Loads the dependencies of a private VLC installation before vlcj loads libvlc, for installations whose
// dependencies are not on the system, such as the Debian packages of 64-bit ARM Linux. System discovery is left
// alone, because a system VLC finds its dependencies itself.
final class PreloadingVlcDiscovery implements VlcDiscovery {

  private final Runnable preloader;
  private final VlcDiscovery delegate;

  PreloadingVlcDiscovery(final Runnable preloader, final VlcDiscovery delegate) {
    this.preloader = preloader;
    this.delegate = delegate;
  }

  @Override
  public boolean discoverSystemInstallation() {
    return this.delegate.discoverSystemInstallation();
  }

  @Override
  public @Nullable Path getDiscoveredPath() {
    return this.delegate.getDiscoveredPath();
  }

  @Override
  public boolean loadInstalledLibraries(final Path libraryDirectory) {
    this.preloader.run();
    return this.delegate.loadInstalledLibraries(libraryDirectory);
  }
}
