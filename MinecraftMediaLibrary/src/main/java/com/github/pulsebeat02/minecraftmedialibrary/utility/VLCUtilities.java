package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.exception.UnsupportedOperatingSystemException;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.MMLNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.LinuxNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac.MacNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows.WindowsNativeDiscovery;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class VLCUtilities {

  private VLCUtilities() {}

  /**
   * Checks if VLC installation exists or not.
   *
   * @param directory the library
   * @return whether vlc can be found or not
   */
  public static boolean checkVLCExistence(@NotNull final File directory) {
    MMLNativeDiscovery discovery = null;
    if (RuntimeUtilities.isWindows()) {
      discovery = new WindowsNativeDiscovery();
    } else if (RuntimeUtilities.isMac()) {
      discovery = new MacNativeDiscovery();
    } else if (RuntimeUtilities.isLinux()) {
      discovery = new LinuxNativeDiscovery();
    }
    if (discovery == null) {
      throw new UnsupportedOperatingSystemException(
          "Couldn't find the correct method to discover VLC!");
    }
    return discovery.discover(directory);
  }
}
