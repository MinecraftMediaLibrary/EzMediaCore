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
    final FileConfiguration configuration = getFileConfiguration();
    final boolean enabled = configuration.getBoolean("enabled");
    final int port = configuration.getInt("port");
    final String directory =
        String.format(
            "%s/%s",
            getPlugin().getDataFolder().getAbsolutePath(), configuration.getString("directory"));
    final String header = configuration.getString("header");
    final boolean verbose = configuration.getBoolean("verbose");
    if (enabled) {
      daemon = new HttpDaemonProvider(directory, port);
      final HttpFileDaemonServer http = daemon.getDaemon();
      if (header == null) {
        Logger.info(
            "Invalid Header in httpserver.yml! Can only be ZIP or OCTET-STREAM. Resorting to ZIP.");
      }
      http.setZipHeader(
          header == null || header.equals("ZIP")
              ? HttpFileDaemonServer.ZipHeader.ZIP
              : HttpFileDaemonServer.ZipHeader.OCTET_STREAM);
      http.setVerbose(verbose);
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
