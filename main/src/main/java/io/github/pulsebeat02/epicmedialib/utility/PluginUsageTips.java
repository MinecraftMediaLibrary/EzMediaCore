package io.github.pulsebeat02.epicmedialib.utility;

import io.github.pulsebeat02.epicmedialib.Logger;
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
    if (MAJOR_VERSION < 11) {
      Logger.warn(
          "EpicMediaLib is moving towards a newer Java Version (Java 11) \n"
              + "Please switch as soon as possible before the library will be incompatible \n"
              + "with your server. If you want to read more information surrounding this, \n"
              + "you may want to take a look here at "
              + "https://papermc.io/forums/t/java-11-mc-1-17-and-paper/5615");
    }
  }

  public static void sendPacketCompressionTip() {
    if (Bukkit.getOnlineMode()) {
      Logger.info(
          "Setting the property network-compression-threshold in server.properties to -1 could "
              + "lead to a significant improvement of performance for servers that aren't proxy"
              + " servers.");
    }
  }
}
