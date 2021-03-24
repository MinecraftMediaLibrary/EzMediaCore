/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.logger;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The custom logger implementation that is used throughout the library. The log file can be found
 * in the base server folder called "mml.log", and contains very useful information about the
 * execution of the library.
 */
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
