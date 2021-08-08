package io.github.pulsebeat02.ezmediacore.vlc;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VLCBinaryLocator implements BinaryLocator {

  private final MediaLibraryCore core;
  private final BinarySearcher searcher;
  private final Path path;
  private final BinaryInstaller installer;

  public VLCBinaryLocator(@NotNull final MediaLibraryCore core, @NotNull final Path path) {
    this(core, path, null, null);
  }

  public VLCBinaryLocator(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final BinarySearcher searcher) {
    this(core, path, searcher, null);
  }

  public VLCBinaryLocator(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final BinaryInstaller installer) {
    this(core, path, null, installer);
  }

  public VLCBinaryLocator(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @Nullable final BinarySearcher searcher,
      @Nullable final BinaryInstaller installer) {
    this.core = core;
    this.path = path;
    this.searcher = searcher == null ? new NativeBinarySearch(core, path) : searcher;
    this.installer = installer == null ? new NativeBinaryInstaller(core) : installer;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull Optional<Path> locate() throws IOException, InterruptedException {

    final Optional<Path> search = this.searcher.search();
    if (search.isEmpty()) {
      this.installer.download();
      return Optional.of(this.installer.getProvider().getInstallationPath());
    }

    return search;
  }

  @Override
  public @NotNull Path getPath() {
    return this.path;
  }

  @Override
  public @NotNull BinaryInstaller getInstaller() {
    return this.installer;
  }

  @Override
  public @NotNull BinarySearcher getSearcher() {
    return this.searcher;
  }
}
