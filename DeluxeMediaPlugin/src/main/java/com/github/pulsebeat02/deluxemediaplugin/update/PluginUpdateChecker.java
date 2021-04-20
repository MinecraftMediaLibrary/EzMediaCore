/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

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
