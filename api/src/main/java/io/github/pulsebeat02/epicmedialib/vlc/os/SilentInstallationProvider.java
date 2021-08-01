package io.github.pulsebeat02.epicmedialib.vlc.os;

import io.github.pulsebeat02.epicmedialib.LibraryInjectable;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface SilentInstallationProvider extends LibraryInjectable, VLCBinaryInstallation {
  @NotNull
  Path getDirectory();

  void setInstallationPath(@NotNull final Path path);

  void deleteArchive(@NotNull final Path archive);
}
