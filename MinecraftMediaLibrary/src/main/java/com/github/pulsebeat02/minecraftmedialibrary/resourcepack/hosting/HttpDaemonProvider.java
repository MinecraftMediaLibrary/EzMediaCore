/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting;

import com.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemon;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class HttpDaemonProvider extends AbstractHostingProvider {

  private static final String SERVER_IP = Bukkit.getIp();
  private final int port;
  private HttpDaemon daemon;

  /**
   * Instantiates a new Http daemon provider.
   *
   * @param path the path
   * @param port the port
   */
  public HttpDaemonProvider(@NotNull final String path, final int port) {
    this.port = port;
    try {
        daemon = new HttpDaemon(port, path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets server ip.
   *
   * @return the server ip
   */
  public static String getServerIp() {
    return SERVER_IP;
  }

  /** Start server. */
  public void startServer() {
    daemon.start();
  }

  /**
   * Generates the URL based on file (String)
   *
   * @param file to generate parent directory of the HTTP Server for.
   * @return file url
   */
  @Override
  public String generateUrl(@NotNull final String file) {
    return "http://" + SERVER_IP + ":" + port + "/" + file;
  }

  /**
   * Generates the URL based on file (Path)
   *
   * @param path to gnerate parent directory of the HTTP Server for.
   * @return file url
   */
  @Override
  public String generateUrl(@NotNull final Path path) {
    return "http://" + SERVER_IP + ":" + port + "/" + path.getFileName();
  }

  /**
   * Gets daemon.
   *
   * @return the daemon
   */
  public HttpDaemon getDaemon() {
    return daemon;
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  public int getPort() {
    return port;
  }
}
