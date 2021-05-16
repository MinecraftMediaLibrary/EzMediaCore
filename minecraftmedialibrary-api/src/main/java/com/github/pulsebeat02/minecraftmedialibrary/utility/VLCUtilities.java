package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.exception.UnsupportedOperatingSystemException;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.MMLNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.WellKnownDirectoryProvider;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.LinuxKnownDirectories;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.LinuxNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac.MacKnownDirectories;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac.MacNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows.WindowsKnownDirectories;
import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows.WindowsNativeDiscovery;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.util.Set;

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
    WellKnownDirectoryProvider provider = null;
    if (RuntimeUtilities.isWindows()) {
      discovery = new WindowsNativeDiscovery();
      provider = new WindowsKnownDirectories();
    } else if (RuntimeUtilities.isMac()) {
      discovery = new MacNativeDiscovery();
      provider = new MacKnownDirectories();
    } else if (RuntimeUtilities.isLinux()) {
      discovery = new LinuxNativeDiscovery();
      provider = new LinuxKnownDirectories();
    }
    if (discovery == null) {
      throw new UnsupportedOperatingSystemException(
          "Couldn't find the correct method to discover VLC!");
    }
    final Set<String> paths = provider.search();
    paths.add(directory.getAbsolutePath());
    for (final String path : paths) {
      if (discovery.discover(new File(path))) {
        return true;
      }
    }
    // last resort
    return new NativeDiscovery().discover();
  }
}
