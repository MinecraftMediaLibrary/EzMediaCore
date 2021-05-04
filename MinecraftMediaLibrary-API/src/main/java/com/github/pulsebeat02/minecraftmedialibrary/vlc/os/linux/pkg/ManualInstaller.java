package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.pkg;

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
    super(file, true);
  }

  /**
   * Installs the packages accordingly.
   */
  @Override
  public void installPackage() {
    Logger.info("Please look at the message in the logger...");
  }

  /**
   * Uses any steps to setup a package.
   */
  @Override
  public void setupPackage() {}
}
