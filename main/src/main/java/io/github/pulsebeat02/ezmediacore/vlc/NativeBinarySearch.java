package io.github.pulsebeat02.ezmediacore.vlc;

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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class NativeBinarySearch implements BinarySearcher {

  private final MediaLibraryCore core;
  private final OSType type;
  private final Path search;
  private final DiscoveryProvider provider;
  private final Collection<WellKnownDirectoryProvider> directories;

  private Path path;

  public NativeBinarySearch(@NotNull final MediaLibraryCore core, @NotNull final Path search) {
    this.core = core;
    this.type = core.getDiagnostics().getSystem().getOSType();
    this.search = search;
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
      case MAC -> new EMCNativeDiscovery(this.core, new MacNativeDiscovery(), true);
      case UNIX -> new EMCNativeDiscovery(this.core, new UnixNativeDiscovery(), true);
      case WINDOWS -> new EMCNativeDiscovery(this.core, new WindowsNativeDiscovery(), false);
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
  public Optional<Path> search() throws IOException {

    if (this.path != null) {
      return Optional.of(this.path);
    }

    final List<String> paths = this.getSearchDirectories();
    paths.add(this.search.toString());

    for (final String path : paths) {
      final Optional<Path> results = this.provider.discover(Paths.get(path));
      if (results.isPresent()) {
        this.path = results.get();
        return Optional.of(this.path);
      }
    }

    return Optional.empty();
  }

  private List<String> getSearchDirectories() {
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
