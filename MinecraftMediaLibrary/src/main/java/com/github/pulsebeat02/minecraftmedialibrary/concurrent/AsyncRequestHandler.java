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

import com.github.pulsebeat02.minecraftmedialibrary.http.RequestHandlerBase;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An async helper class used for request handling.
 */
public class AsyncRequestHandler {

  private final RequestHandlerBase request;

  /**
   * Instantiates a new AsyncRequestHandler.
   *
   * @param request the request
   */
  public AsyncRequestHandler(@NotNull final RequestHandlerBase request) {
    this.request = request;
  }

  /**
   * Handles the request in a CompletableFuture.
   *
   * @return the CompletableFuture
   */
  public CompletableFuture<Void> handleRequest() {
    return CompletableFuture.runAsync(request::handleRequest);
  }
}
