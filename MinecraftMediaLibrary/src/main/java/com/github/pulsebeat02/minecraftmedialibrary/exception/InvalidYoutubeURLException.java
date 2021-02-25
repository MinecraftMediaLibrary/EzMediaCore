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

package com.github.pulsebeat02.minecraftmedialibrary.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidYoutubeURLException extends AssertionError {

  private static final long serialVersionUID = -6428433369003844013L;

  /**
   * Instantiates a new InvalidYoutubeURLException.
   *
   * @param message the exception message
   */
  public InvalidYoutubeURLException(@NotNull final String message) {
    super(message);
  }

  @Override
  public synchronized Throwable getCause() {
    return this;
  }

  @Override
  public synchronized Throwable initCause(@NotNull final Throwable cause) {
    return this;
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
