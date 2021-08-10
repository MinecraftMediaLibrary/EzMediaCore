package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class PluginUsageTips {

  private static final String JAVA_VERSION;
  private static final int MAJOR_VERSION;

  static {
    JAVA_VERSION = System.getProperty("java.version");
    MAJOR_VERSION = Integer.parseInt(JAVA_VERSION.split("\\.")[1]);
  }

  private PluginUsageTips() {
  }

  public static String getJavaVersion() {
    return JAVA_VERSION;
  }

  public static int getMajorVersion() {
    return MAJOR_VERSION;
  }

  public static void sendWarningMessage() {
  }

  public static void sendPacketCompressionTip() {
    if (Bukkit.getOnlineMode()) {
      Logger.warn(
          """
              Setting the value "network-compression-threshold", to -1 in the server.properties
               file may lead to improved performance of video players for servers that aren't proxy
               servers.
              """
      );
    }
  }

  public static void sendSpotifyWarningMessage(@NotNull final MediaLibraryCore core) {
    if (core.getSpotifyClient() == null) {
      Logger.warn(
          """
              Spotify API Client ID and Client Secret not specified! You will not be able
               to use any Spotify related features.
              """
      );
    }
  }

}
