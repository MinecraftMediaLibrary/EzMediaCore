package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
    LOGGER.write(line);
    LOGGER.flush();
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
