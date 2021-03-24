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

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * The base handler of requests. Used for creating custom request managers. Used within the
 * MinecraftMediaLibrary to handle requests to an HTTP daemon.
 */
public interface RequestHandlerBase {

  /**
   * Creates a header for the HTTP request. Useful for certain connections.
   *
   * @param file to create header for
   * @return Header of the specified file
   */
  String buildHeader(@NotNull final File file);

  /**
   * Handles the incoming request accordingly. Warning: Overriding this requires a rewrite of the
   * incoming connection
   */
  void handleRequest();
}
