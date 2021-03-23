package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class LinuxSilentInstallation extends SilentOSDependentSolution {

  // TODO: Finish LinuxSilentInstallation class

  public LinuxSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  public LinuxSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public boolean checkVLCExistance(@NotNull final String dir) {
    return false;
  }

  @Override
  public void downloadVLCLibrary() throws IOException {}

  @Override
  public void loadNativeDependency(@Nullable final File folder) {}
}
