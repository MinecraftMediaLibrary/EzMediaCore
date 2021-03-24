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

package com.github.pulsebeat02.minecraftmedialibrary.logger;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

  /** Tracks whether log should be verbose */
  public static boolean VERBOSE;
  protected static volatile BufferedWriter WRITER;

  static {
    try {
      final File f = new File("mml.log");
      System.out.println(f.getAbsolutePath());
      if (f.createNewFile()) {
        System.out.println("File Created (" + f.getName() + ")");
      } else {
        System.out.println("Log File Exists Already");
      }
      WRITER = new BufferedWriter(new FileWriter(f, false));
    } catch (final IOException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Prints the text as an [INFO]
   *
   * @param info the info
   */
  public static void info(@NotNull final Object info) {
    directPrint(System.currentTimeMillis() + ": [INFO] " + info + "\n");
  }

  /**
   * Prints the text as a [WARN]
   *
   * @param warning the warning
   */
  public static void warn(@NotNull final Object warning) {
    directPrint(System.currentTimeMillis() + ": [WARN] " + warning + "\n");
  }

  /**
   * Prints the text as a [ERROR]
   *
   * @param error the error
   */
  public static void error(@NotNull final Object error) {
    directPrint(System.currentTimeMillis() + ": [ERROR] " + error + "\n");
  }

  /**
   * Directly prints the following line.
   *
   * @param line to print
   */
  protected static void directPrint(@NotNull final String line) {
    if (VERBOSE) {
      try {
        WRITER.write(line);
        WRITER.flush();
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
    }
  }

  /**
   * Sets verbosity of logger.
   *
   * @param verbose verbosity
   */
  public static void setVerbose(final boolean verbose) {
    VERBOSE = verbose;
  }
}
