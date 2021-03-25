package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.pkg;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ManualInstaller extends PackageBase {

  /**
   * Instantiates a new ManualInstaller.
   *
   * @param file the file
   */
  public ManualInstaller(@NotNull final File file) {
    super(file);
  }

  /**
   * Installs the packages accordingly.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void installPackage() throws IOException {
    Logger.info("Please look at the message in the logger...");
  }

  /**
   * Uses any steps to setup a package.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void setupPackage() throws IOException {}
}
