package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.pkg;

import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/** Extracts the package binaries instead of other methods. */
public class ExtractionInstaller extends PackageBase {

  /**
   * Instantiates a new ExtractionInstaller.
   *
   * @param file the file
   */
  public ExtractionInstaller(@NotNull final File file) {
    super(file, true);
  }

  /**
   * Installs the packages accordingly.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void installPackage() throws IOException {
    final File f = getFile();
    ArchiveUtilities.recursiveExtraction(f, f.getParentFile());
  }

  /**
   * Uses any steps to setup a package.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void setupPackage() throws IOException {}
}
