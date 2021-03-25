package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.pkg;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/** Extracts the package binaries instead of other methods. */
public class ExtractionInstaller extends PackageInstaller {

  /**
   * Instantiates a new PackageInstaller.
   *
   * @param pkg the package
   * @param file the file
   */
  public ExtractionInstaller(@NotNull LinuxPackage pkg, @NotNull File file) {
    super(pkg, file);
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
