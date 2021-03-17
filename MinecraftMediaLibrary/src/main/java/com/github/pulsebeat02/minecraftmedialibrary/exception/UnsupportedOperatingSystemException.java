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

package com.github.pulsebeat02.minecraftmedialibrary.exception;

import org.jetbrains.annotations.NotNull;

public final class UnsupportedOperatingSystemException extends AssertionError {

  private static final long serialVersionUID = 1682368011870345698L;

  /**
   * Instantiates a new UnsupportedOperatingSystemException.
   *
   * @param message the exception message
   */
  public UnsupportedOperatingSystemException(@NotNull final String message) {
    super(message);
  }

  /**
   * Gets the cause of the exception.
   *
   * @return this
   */
  @Override
  public synchronized Throwable getCause() {
    return this;
  }

  /**
   * Initializes the cause of the exception.
   *
   * @param cause cause
   * @return this
   */
  @Override
  public synchronized Throwable initCause(@NotNull final Throwable cause) {
    return this;
  }

  /**
   * Fills in stack trace for exception.
   *
   * @return throwable stack trace
   */
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
