package io.github.pulsebeat02.ezmediacore.vlc.os.window;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.WellKnownDirectoryProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

public class WindowsKnownDirectories implements WellKnownDirectoryProvider {

  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.WINDOWS;
  }

  @Override
  public @NotNull Collection<String> getSearchDirectories() {
    return new HashSet<>(
        Collections.singleton(
            Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\VideoLAN\\VLC", "InstallDir")));
  }
}
