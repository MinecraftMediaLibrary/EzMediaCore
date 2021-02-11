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

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.http.AbstractHttpDaemon;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncHttpDaemonBooter {

  private final AbstractHttpDaemon daemon;

  public AsyncHttpDaemonBooter(@NotNull final AbstractHttpDaemon daemon) {
    this.daemon = daemon;
  }

  public CompletableFuture<Void> startServer() {
    return CompletableFuture.runAsync(daemon::startServer);
  }
}
