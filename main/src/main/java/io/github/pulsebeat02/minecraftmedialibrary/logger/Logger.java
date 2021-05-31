/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
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

package io.github.pulsebeat02.minecraftmedialibrary.logger;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The custom logger implementation that is used throughout the library. The log file can be found
 * in the base server folder called "mml.log", and contains very useful information about the
 * execution of the library.
 */
public final class Logger {

  /** Tracks whether log should be verbose */
  protected static boolean VERBOSE;

  protected static volatile PrintWriter WRITER;
  protected static Path LOG_FILE;

  public static void initializeLogger(@NotNull final MediaLibrary library) {
    try {
      final Path folder = Paths.get(library.getPlugin().getDataFolder().toString()).resolve("mml");
      LOG_FILE = folder.resolve("mml.log");
      Files.createDirectories(folder);
      Files.createFile(LOG_FILE);
      WRITER = new PrintWriter(new FileWriter(LOG_FILE.toFile()), true);
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
    directPrint(String.format("%d: [INFO] %s\n", System.currentTimeMillis(), info));
  }

  /**
   * Prints the text as a [WARN]
   *
   * @param warning the warning
   */
  public static void warn(@NotNull final Object warning) {
    directPrint(String.format("%d: [WARN] %s\n", System.currentTimeMillis(), warning));
  }

  /**
   * Prints the text as a [ERROR]
   *
   * @param error the error
   */
  public static void error(@NotNull final Object error) {
    directPrint(String.format("%d: [ERROR] %s\n", System.currentTimeMillis(), error));
  }

  /**
   * Directly prints the following line.
   *
   * @param line to print
   */
  private static void directPrint(@NotNull final String line) {
    if (VERBOSE) {
      WRITER.write(line);
      WRITER.flush();
    }
  }

  /**
   * Gets whether the Logger is verbose or not.
   *
   * @return whether the logger is verbose
   */
  public static boolean isVerbose() {
    return VERBOSE;
  }

  /**
   * Sets verbosity of logger.
   *
   * @param verbose verbosity
   */
  public static void setVerbose(final boolean verbose) {
    VERBOSE = verbose;
  }

  /**
   * Gets the PrintWriter associated with the Logger.
   *
   * @return the BufferedWriter
   */
  public static PrintWriter getWriter() {
    return WRITER;
  }

  /**
   * Gets the File associated with the Logger file.
   *
   * @return the log file
   */
  public static Path getLogFile() {
    return LOG_FILE;
  }
}
