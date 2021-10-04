/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class PluginUsageTips {

  private PluginUsageTips() {
  }

  public static void sendWarningMessage() {
    Logger.warn("""
        As a reminder, the only server softwares supported by this library are Spigot and Paper.
        Tunity has been merged into Paper and will not be supported. Custom forks will also not
        be supported. If you can reproduce an issue on Spigot or Paper 1.17.1, I am welcome to
        accept and analyze it. Otherwise, I cannot do anything. This message is automatically
        sent to all servers.
        """);
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
