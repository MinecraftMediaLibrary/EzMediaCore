/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.utility;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/** A set of utilities which allow the better debugging output of the media library. */
public final class DebuggerUtilities {

  private DebuggerUtilities() {}

  /**
   * Gets the debug information about a plugin using the library.
   *
   * @param library the librar
   */
  public static void getDebugInformation(@NotNull final MediaLibrary library) {
    CompletableFuture.runAsync(
        () -> {
          final Plugin plugin = library.getPlugin();
          final Server server = plugin.getServer();
          Logger.info("===========================================");
          Logger.info("             DEBUG FILE LOGGERS            ");
          Logger.info("===========================================");
          Logger.info("             PLUGIN INFORMATION            ");
          Logger.info("===========================================");
          Logger.info(String.format("Plugin Name: %s", plugin.getName()));
          Logger.info(String.format("Plugin Description: %s", plugin.getDescription()));
          Logger.info(String.format("HTTP Server Path: %s", library.getHttpParentFolder()));
          Logger.info(String.format("Using VLC (VLC Media Player)? %s", library.isVlcj()));
          Logger.info(String.format("Library Disabled? %s", library.isDisabled()));
          Logger.info(String.format("Library Path: %s", library.getPath()));
          Logger.info(String.format("VLC Path: %s", library.getVlcFolder()));
          Logger.info(String.format("Image Path: %s", library.getImageFolder()));
          Logger.info(String.format("Audio Path: %s", library.getAudioFolder()));
          Logger.info("===========================================");
          Logger.info("             SERVER INFORMATION            ");
          Logger.info("===========================================");
          Logger.info(String.format("Server Name: %s", server.getName()));
          Logger.info(String.format("Version: %s", server.getVersion()));
          Logger.info(String.format("Idle Timeout: %d", server.getIdleTimeout()));
          Logger.info(String.format("Online Mode?: %s", server.getOnlineMode()));
          Logger.info(String.format("Player Count: %s", server.getOnlinePlayers()));
          Logger.info(
              "Plugins: "
                  + Arrays.stream(server.getPluginManager().getPlugins())
                      .map(Plugin::getName)
                      .collect(Collectors.toList()));
          Logger.info("===========================================");
          Logger.info("             SYSTEM INFORMATION            ");
          Logger.info("===========================================");
          Logger.info(String.format("Operating System: %s", RuntimeUtilities.getOperatingSystem()));
          Logger.info(String.format("Version: %s", System.getProperty("os.version")));
          Logger.info(String.format("CPU Architecture: %s", RuntimeUtilities.getCpuArch()));
          Logger.info(String.format("Windows: %s", RuntimeUtilities.isWindows()));
          Logger.info(String.format("Mac: %s", RuntimeUtilities.isMac()));
          Logger.info(String.format("Linux: %s", RuntimeUtilities.isLinux()));
          Logger.info(
              String.format("Linux Distribution: %s", RuntimeUtilities.getLinuxDistribution()));
          Logger.info(String.format("VLC Installation URL: %s", RuntimeUtilities.getVLCUrl()));
          Logger.info(
              String.format("FFmpeg Installation URL: %s", RuntimeUtilities.getFFmpegUrl()));
          Logger.info("===========================================");
        });
  }
}
