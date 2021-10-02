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

package io.github.pulsebeat02.deluxemediaplugin.update;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public final class UpdateChecker {

  private final DeluxeMediaPlugin plugin;
  private int resource;

  public UpdateChecker(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    try {
      this.resource = Integer.parseInt("%%__RESOURCE__%%");
    } catch (final NumberFormatException e) {
      this.resource = -1;
    }
  }

  public void check() {
    CompletableFuture.runAsync(this::request);
  }

  private void request() {
    final Audience console = this.plugin.getLogger();
    try (final Scanner scanner =
        new Scanner(
            new URL(
                "https://api.spigotmc.org/legacy/update.php?resource=%d"
                    .formatted(this.resource))
                .openStream())) {
      final String update = scanner.next();
      if (this.plugin.getBootstrap().getDescription().getVersion().equalsIgnoreCase(update)) {
        console.sendMessage(Locale.NEW_UPDATE_PLUGIN.build(update));
      } else {
        console.sendMessage(Locale.RUNNING_LATEST_PLUGIN.build());
      }
    } catch (final IOException exception) {
      console.sendMessage(Locale.ERR_CANNOT_CHECK_UPDATES.build(exception.getMessage()));
    }
  }
}
