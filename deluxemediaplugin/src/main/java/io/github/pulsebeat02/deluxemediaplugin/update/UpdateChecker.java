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
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class UpdateChecker {

  private final DeluxeMediaPlugin plugin;
  private final int resource;

  public UpdateChecker(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    this.resource = this.getResourceID();
  }

  private int getResourceID() {
    try {
      return Integer.parseInt("%%__RESOURCE__%%");
    } catch (final NumberFormatException e) {
      return -1;
    }
  }

  public void check() {
    final Audience console = this.plugin.getConsoleAudience();
    final String url =
        "https://api.spigotmc.org/legacy/update.php?resource=%d".formatted(this.resource);
    this.fetchRequest(console, url);
  }

  private void fetchRequest(@NotNull final Audience console, @NotNull final String url) {
    try (final InputStream is = new URL(url).openStream();
        final Scanner scanner = new Scanner(is)) {
      console.sendMessage(this.getMessage(scanner.next()));
    } catch (final IOException e) {
      console.sendMessage(Locale.ERR_CANNOT_CHECK_UPDATES.build(e.getMessage()));
    }
  }

  private @NotNull Component getMessage(@NotNull final String update) {
    return this.checkNewUpdate(update)
        ? Locale.NEW_UPDATE_PLUGIN.build(update)
        : Locale.RUNNING_LATEST_PLUGIN.build();
  }

  private boolean checkNewUpdate(@NotNull final String update) {
    return this.plugin.getBootstrap().getDescription().getVersion().equalsIgnoreCase(update);
  }
}
