/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public final class Logger {

  private static PrintWriter LOGGER;
  private static Path LOG_FILE;

  public static void init(@NotNull final MediaLibraryCore core) {
    LOG_FILE = core.getLibraryPath().resolve("emc.log");
    try {
      Files.createDirectories(LOG_FILE.getParent());
      FileUtils.createIfNotExists(LOG_FILE);
      LOGGER = new PrintWriter(new FileWriter(LOG_FILE.toFile()), true);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Prints the text as an [INFO]
   *
   * @param info the info
   */
  public static synchronized void info(@NotNull final Object info) {
    directPrint("%d: [INFO] %s\n".formatted(System.currentTimeMillis(), info));
  }

  /**
   * Prints the text as a [WARN]
   *
   * @param warning the warning
   */
  public static synchronized void warn(@NotNull final Object warning) {
    directPrint("%d: [WARN] %s\n".formatted(System.currentTimeMillis(), warning));
  }

  /**
   * Prints the text as a [ERROR]
   *
   * @param error the error
   */
  public static synchronized void error(@NotNull final Object error) {
    directPrint("%d: [ERROR] %s\n".formatted(System.currentTimeMillis(), error));
  }

  /**
   * Directly prints the following line.
   *
   * @param line to print
   */
  private static synchronized void directPrint(@NotNull final String line) {
    CompletableFuture.runAsync(() -> {
      LOGGER.write(line);
      LOGGER.flush();
    }, ExecutorProvider.SHARED_RESULT_POOL);
  }

  /**
   * Gets the File associated with the Logger file.
   *
   * @return the log file
   */
  public static Path getLoggerPath() {
    return LOG_FILE;
  }
}
