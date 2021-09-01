/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.vlc;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.DiscoveryProvider;
import io.github.pulsebeat02.ezmediacore.vlc.os.WellKnownDirectoryProvider;
import io.github.pulsebeat02.ezmediacore.vlc.os.mac.MacKnownDirectories;
import io.github.pulsebeat02.ezmediacore.vlc.os.mac.MacNativeDiscovery;
import io.github.pulsebeat02.ezmediacore.vlc.os.unix.UnixKnownDirectories;
import io.github.pulsebeat02.ezmediacore.vlc.os.unix.UnixNativeDiscovery;
import io.github.pulsebeat02.ezmediacore.vlc.os.window.WindowsKnownDirectories;
import io.github.pulsebeat02.ezmediacore.vlc.os.window.WindowsNativeDiscovery;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class NativeBinarySearch implements BinarySearcher {

  private final MediaLibraryCore core;
  private final OSType type;
  private final DiscoveryProvider provider;
  private final Collection<WellKnownDirectoryProvider> directories;

  private Path search;
  private Path path;

  public NativeBinarySearch(@NotNull final MediaLibraryCore core, @NotNull final Path search) {
    this.core = core;
    this.type = core.getDiagnostics().getSystem().getOSType();
    this.search = search;
    this.provider = this.getDiscovery();
    this.directories = this.getDirectories();
  }

  public NativeBinarySearch(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.type = core.getDiagnostics().getSystem().getOSType();
    this.provider = this.getDiscovery();
    this.directories = this.getDirectories();
  }

  public NativeBinarySearch(
      @NotNull final MediaLibraryCore core,
      @NotNull final DiscoveryProvider custom,
      @NotNull final Path search) {
    this.core = core;
    this.type = core.getDiagnostics().getSystem().getOSType();
    this.search = search;
    this.provider = custom;
    this.directories = this.getDirectories();
  }

  @NotNull
  private Collection<WellKnownDirectoryProvider> getDirectories() {
    return new ArrayList<>(Collections.singleton(switch (this.type) {
      case MAC -> new MacKnownDirectories();
      case UNIX -> new UnixKnownDirectories();
      case WINDOWS -> new WindowsKnownDirectories();
    }));
  }

  @NotNull
  private EMCNativeDiscovery getDiscovery() {
    return switch (this.type) {
      case MAC -> new EMCNativeDiscovery(this.core, new MacNativeDiscovery());
      case UNIX -> new EMCNativeDiscovery(this.core, new UnixNativeDiscovery());
      case WINDOWS -> new EMCNativeDiscovery(this.core, new WindowsNativeDiscovery());
    };
  }

  @Override
  public @NotNull DiscoveryProvider getDiscoveryMethod() {
    return this.provider;
  }

  @Override
  public @NotNull Path getSearchPath() {
    return this.search;
  }

  @Override
  public @NotNull Path getVlcPath() {
    return this.path;
  }

  @Override
  public @NotNull Collection<WellKnownDirectoryProvider> getWellKnownDirectories() {
    return this.directories;
  }

  @Override
  public Optional<Path> search() {

    if (this.path != null) {
      return Optional.of(this.path);
    }

    final List<String> paths = this.getSearchDirectories();
    if (this.search != null) {
      paths.add(this.search.toString());
    }

    for (final String path : paths) {
      final Optional<Path> optional = this.provider.discover(Path.of(path));
      if (optional.isPresent()) {
        this.path = optional.get();
        break;
      }
    }

    Logger.info(this.path == null ? "VLC path is invalid!" : "VLC path is valid!");

    return Optional.ofNullable(this.path);
  }

  private @NotNull List<String> getSearchDirectories() {
    final List<String> paths = new ArrayList<>();
    for (final WellKnownDirectoryProvider directory : this.directories) {
      paths.addAll(directory.getSearchDirectories());
    }
    return paths;
  }

  @Override
  public boolean addDiscoveryDirectory(@NotNull final WellKnownDirectoryProvider provider) {
    if (this.type == provider.getOperatingSystem()) {
      this.directories.add(provider);
      return true;
    }
    return false;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
