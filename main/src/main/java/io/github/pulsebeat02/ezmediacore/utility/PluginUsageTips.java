package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.Logger;
import org.bukkit.Bukkit;

public final class PluginUsageTips {

  private static final String JAVA_VERSION;
  private static final int MAJOR_VERSION;

  static {
    JAVA_VERSION = System.getProperty("java.version");
    MAJOR_VERSION = Integer.parseInt(JAVA_VERSION.split("\\.")[1]);
  }

  private PluginUsageTips() {}

  public static String getJavaVersion() {
    return JAVA_VERSION;
  }

  public static int getMajorVersion() {
    return MAJOR_VERSION;
  }

  public static void sendWarningMessage() {
    if (MAJOR_VERSION < 16) {
      Logger.warn(
              """
                      EzMediaCore has made a full transition to Java 16! The library is incompatible
                       with your current version. Consider upgrading your Java version or else the
                       plugin using the library will be incompatible. For more information surrounding
                       this, please visit https://papermc.io/forums/t/java-11-mc-1-17-and-paper/5615.
                      """
      );
    }
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
}
