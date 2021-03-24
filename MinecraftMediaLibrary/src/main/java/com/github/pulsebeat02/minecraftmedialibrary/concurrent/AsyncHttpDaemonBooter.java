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

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemonBase;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An async helper class used for booting HttpDaemons.
 */
public class AsyncHttpDaemonBooter {

  private final HttpDaemonBase daemon;

  /**
   * Instantiates a new AsyncHttpDaemonBooter.
   *
   * @param daemon the daemon
   */
  public AsyncHttpDaemonBooter(@NotNull final HttpDaemonBase daemon) {
    this.daemon = daemon;
  }

  /**
   * Start the server in a CompletableFuture
   *
   * @return the CompletableFuture
   */
  public CompletableFuture<Void> startServer() {
    return CompletableFuture.runAsync(daemon::startServer);
  }
}
