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

package com.github.pulsebeat02.minecraftmedialibrary.http;

import java.net.Socket;

/**
 * An interface to specify custom Http Daemon classes. Used within the MinecraftMediaLibrary as
 * well.
 */
public interface HttpDaemonBase {

  /** Method used to start the HTTP Daemon. */
  void startServer();

  /** Called right before the HTTP Daemon starts running. */
  void onServerStart();

  /** Called right before the HTTP Daemon terminates. */
  void onServerTerminate();

  /**
   * Called when an incoming user connects to the HTTP Server.
   *
   * @param client for the incoming connection.
   */
  void onClientConnect(final Socket client);

  /**
   * Called when a resourcepack failed to be installed for a user.
   *
   * @param socket for the connection which failed download.
   */
  void onResourcepackFailedDownload(final Socket socket);
}