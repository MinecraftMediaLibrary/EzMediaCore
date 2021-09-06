package io.github.pulsebeat02.ezmediacore;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class VLCWindowsRegistryTest {

  public static void main(final String[] args) {
    final String directory =
        Advapi32Util.registryGetStringValue(
            WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\VideoLAN\\VLC", "InstallDir");
    final String ver =
        Advapi32Util.registryGetStringValue(
            WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\VideoLAN\\VLC", "Version");
    System.out.println(directory);
    System.out.println(ver);
  }
}
