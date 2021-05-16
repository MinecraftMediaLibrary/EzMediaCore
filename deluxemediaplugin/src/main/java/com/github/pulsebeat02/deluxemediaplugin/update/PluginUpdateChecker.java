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

package com.github.pulsebeat02.deluxemediaplugin.update;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PluginUpdateChecker {

  private final DeluxeMediaPlugin plugin;
  private final int resource;

  public PluginUpdateChecker(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    resource = -1;
  }

  public void checkForUpdates() {
    final Logger logger = plugin.getLogger();
    logger.info("Checking for Updates...");
    getLatestVersion(
        version -> {
          if (plugin.getDescription().getVersion().equalsIgnoreCase(version)) {
            logger.info("You are running the latest version of DeluxeMediaPlugin. Good job!");
          } else {
            logger.info(
                "There is a new update available. Please update as soon as possible for bug fixes.");
          }
        });
    logger.info("Finished Checking for Updates...");
  }

  private void getLatestVersion(final Consumer<String> consumer) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              try (final InputStream inputStream =
                      new URL(
                              String.format(
                                  "https://api.spigotmc.org/legacy/update.php?resource=%d",
                                  resource))
                          .openStream();
                  final Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                  consumer.accept(scanner.next());
                }
              } catch (final IOException exception) {
                plugin
                    .getLogger()
                    .info(String.format("Cannot look for updates: %s", exception.getMessage()));
              }
            });
  }
}
