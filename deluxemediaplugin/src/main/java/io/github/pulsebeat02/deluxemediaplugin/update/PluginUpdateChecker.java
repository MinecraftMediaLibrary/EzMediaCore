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

package io.github.pulsebeat02.deluxemediaplugin.update;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;

public class PluginUpdateChecker {

  private final DeluxeMediaPlugin plugin;
  private int resource;

  public PluginUpdateChecker(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    try {
      resource = Integer.parseInt("%%__RESOURCE__%%");
    } catch (final NumberFormatException e) {
      resource = -1;
    }
  }

  public void check() {
    CompletableFuture.runAsync(
        () -> {
          try (final Scanner scanner =
              new Scanner(
                  new URL(
                          String.format(
                              "https://api.spigotmc.org/legacy/update.php?resource=%d", resource))
                      .openStream())) {
            final String update = scanner.next();
            if (plugin.getDescription().getVersion().equalsIgnoreCase(update)) {
                plugin.log(String.format("There is a new update available! (%s)", update));
            } else {
                plugin.log("You are currently running the latest version of DeluxeMediaPlugin.");
            }
          } catch (final IOException exception) {
            plugin.log(String.format("Cannot look for updates: %s", exception.getMessage()));
          }
        });
  }
}
