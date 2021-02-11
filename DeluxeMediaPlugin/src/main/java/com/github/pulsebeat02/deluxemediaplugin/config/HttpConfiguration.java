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
import com.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemon;
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
    final HttpDaemon http = daemon.getDaemon();
    configuration.set("directory", http.getDirectory().getAbsolutePath());
    configuration.set(
        "header", http.getHeader() == HttpDaemon.ZipHeader.ZIP ? "ZIP" : "OCTET_STREAM");
    configuration.set("verbose", http.isVerbose());
    saveConfig();
  }

  @Override
  public void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    final boolean enabled = configuration.getBoolean("enabled");
    final int port = configuration.getInt("port");
    final String directory = configuration.getString("directory");
    final String header = configuration.getString("header");
    final boolean verbose = configuration.getBoolean("verbose");
    if (enabled) {
      assert directory != null;
      daemon = new HttpDaemonProvider(directory, port);
      final HttpDaemon http = daemon.getDaemon();
      assert header != null;
      http.setZipHeader(
          header.equals("ZIP") ? HttpDaemon.ZipHeader.ZIP : HttpDaemon.ZipHeader.OCTET_STREAM);
      http.setVerbose(verbose);
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
