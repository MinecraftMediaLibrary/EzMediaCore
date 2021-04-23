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

package com.github.pulsebeat02.deluxemediaplugin.config;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.http.HttpFileDaemonServer;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class HttpConfiguration extends AbstractConfiguration {

  private HttpDaemonProvider daemon;
  private boolean enabled;

  public HttpConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "httpserver.yml");
  }

  @Override
  public void deserialize() {

    // Deserializes the HTTP configuration
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("enabled", enabled);
    configuration.set("port", daemon.getPort());
    final HttpFileDaemonServer http = daemon.getDaemon();
    configuration.set("directory", http.getDirectory().getAbsolutePath());
    configuration.set(
        "header", http.getHeader() == HttpFileDaemonServer.ZipHeader.ZIP ? "ZIP" : "OCTET_STREAM");
    configuration.set("verbose", http.isVerbose());
    saveConfig();
  }

  @Override
  public void serialize() {

    // Reads the HTTP server configuration
    final FileConfiguration configuration = getFileConfiguration();

    // Get whether the server is enabled or not
    final boolean enabled = configuration.getBoolean("enabled");

    // Get the port at which the HTTP server is hosted on (Must be port-forwarded!)
    final int port = configuration.getInt("port");

    // Get the directory at which the HTTP server's root is going to be
    final String directory =
        String.format(
            "%s/%s",
            getPlugin().getDataFolder().getAbsolutePath(), configuration.getString("directory"));

    // Get the proper header for files on the HTTP server
    final String header = configuration.getString("header");

    // Get whether the HTTP server should debug information (requests)
    final boolean verbose = configuration.getBoolean("verbose");

    if (enabled) {

      // Create a new daemon with the specified directory and port
      daemon = new HttpDaemonProvider(directory, port);
      final HttpFileDaemonServer http = daemon.getDaemon();

      // Resort to ZIP if the header isn't valid
      if (header == null) {
        Logger.info(
            "Invalid Header in httpserver.yml! Can only be ZIP or OCTET-STREAM. Resorting to ZIP.");
      }

      // Set the header of the HTTP daemon
      http.setZipHeader(
          header == null || header.equals("ZIP")
              ? HttpFileDaemonServer.ZipHeader.ZIP
              : HttpFileDaemonServer.ZipHeader.OCTET_STREAM);

      // Set the verbosity
      http.setVerbose(verbose);

      // Start the server
      daemon.startServer();
    }
    this.enabled = enabled;
  }

  public HttpDaemonProvider getDaemon() {
    return daemon;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
