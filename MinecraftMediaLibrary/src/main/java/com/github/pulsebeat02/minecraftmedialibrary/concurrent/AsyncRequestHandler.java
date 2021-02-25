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

import com.github.pulsebeat02.minecraftmedialibrary.http.AbstractRequestHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncRequestHandler {

  private final AbstractRequestHandler request;

  /**
   * Instantiates a new Async request handler.
   *
   * @param request the request
   */
  public AsyncRequestHandler(@NotNull final AbstractRequestHandler request) {
    this.request = request;
  }

  /**
   * Handle request completable future.
   *
   * @return the completable future
   */
  public CompletableFuture<Void> handleRequest() {
    return CompletableFuture.runAsync(request::handleRequest);
  }
}
