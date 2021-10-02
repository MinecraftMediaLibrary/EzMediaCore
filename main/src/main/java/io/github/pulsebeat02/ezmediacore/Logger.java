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
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class Logger {

  private static PrintWriter LOGGER;
  private static PrintWriter VLC_LOGGER;
  private static PrintWriter RTP_LOGGER;
  private static PrintWriter FFMPEG_PLAYER_LOGGER;
  private static PrintWriter FFMPEG_STREAMER_LOGGER;

  private static Path LOG_FILE;
  private static Path VLC_LOG_FILE;
  private static Path RTP_LOG_FILE;
  private static Path FFMPEG_PLAYER_LOG_FILE;
  private static Path FFMPEG_STREAM_LOG_FILE;

  public static void init(@NotNull final MediaLibraryCore core) {
    final Path path = core.getLibraryPath();
    LOG_FILE = path.resolve("emc.log");
    VLC_LOG_FILE = path.resolve("vlc.log");
    RTP_LOG_FILE = path.resolve("rtp.log");
    FFMPEG_PLAYER_LOG_FILE = path.resolve("ffmpeg.log");
    FFMPEG_STREAM_LOG_FILE = path.resolve("ffmpeg-stream.log");
    try {
      Files.createDirectories(LOG_FILE.getParent());
      Set.of(LOG_FILE, VLC_LOG_FILE, RTP_LOG_FILE, FFMPEG_PLAYER_LOG_FILE, FFMPEG_STREAM_LOG_FILE)
          .forEach(ThrowingConsumer.unchecked(FileUtils::createIfNotExists));
      LOGGER = new PrintWriter(Files.newBufferedWriter(LOG_FILE), true);
      VLC_LOGGER = new PrintWriter(Files.newBufferedWriter(VLC_LOG_FILE), true);
      RTP_LOGGER = new PrintWriter(Files.newBufferedWriter(RTP_LOG_FILE), true);
      FFMPEG_PLAYER_LOGGER = new PrintWriter(Files.newBufferedWriter(FFMPEG_PLAYER_LOG_FILE), true);
      FFMPEG_STREAMER_LOGGER =
          new PrintWriter(Files.newBufferedWriter(FFMPEG_STREAM_LOG_FILE), true);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void closeAllLoggers() {
    LOGGER.close();
    VLC_LOGGER.close();
    RTP_LOGGER.close();
    FFMPEG_PLAYER_LOGGER.close();
    FFMPEG_STREAMER_LOGGER.close();
  }

  /**
   * Prints the text as an [INFO]
   *
   * @param info the info
   */
  public static void info(@NotNull final Object info) {
    directPrint("%d: [INFO] %s".formatted(System.currentTimeMillis(), info));
  }

  /**
   * Prints the text as a [WARN]
   *
   * @param warning the warning
   */
  public static void warn(@NotNull final Object warning) {
    directPrint("%d: [WARN] %s".formatted(System.currentTimeMillis(), warning));
  }

  /**
   * Prints the text as a [ERROR]
   *
   * @param error the error
   */
  public static void error(@NotNull final Object error) {
    directPrint("%d: [ERROR] %s".formatted(System.currentTimeMillis(), error));
  }

  /**
   * Directly prints the following line.
   *
   * @param line to print
   */
  private static void directPrint(@NotNull final String line) {
    internalDirectPrint(LOGGER, line);
  }

  /**
   * Directly prints the following line into VLC.
   *
   * @param line to print
   */
  public static void directPrintVLC(@NotNull final String line) {
    internalDirectPrint(VLC_LOGGER, line);
  }

  /**
   * Directly prints the following line into FFmpeg player.
   *
   * @param line to print
   */
  public static void directPrintFFmpegPlayer(@NotNull final String line) {
    internalDirectPrint(FFMPEG_PLAYER_LOGGER, line);
  }

  /**
   * Directly prints the following line into FFmpeg stream.
   *
   * @param line to print
   */
  public static void directPrintFFmpegStream(@NotNull final String line) {
    internalDirectPrint(FFMPEG_STREAMER_LOGGER, line);
  }

  /**
   * Directly prints the following line into Rtp.
   *
   * @param line to print
   */
  public static void directPrintRtp(@NotNull final String line) {
    internalDirectPrint(RTP_LOGGER, line);
  }

  private static void internalDirectPrint(
      @NotNull final PrintWriter writer, @NotNull final String line) {
    ExecutorProvider.LOGGER_POOL.submit(
        () -> {
          writer.write("%s\n".formatted(line));
          writer.flush();
        });
  }

  /**
   * Gets the File associated with the Logger file.
   *
   * @return the log file
   */
  public static Path getLoggerPath() {
    return LOG_FILE;
  }

  /**
   * Gets the File associated with the VLC Logger file.
   *
   * @return the log file
   */
  public static Path getVlcLoggerPath() {
    return VLC_LOG_FILE;
  }

  /**
   * Gets the File associated with the FFmpeg Logger file.
   *
   * @return the log file
   */
  public static Path getFFmpegPlayerLoggerPath() {
    return FFMPEG_PLAYER_LOG_FILE;
  }

  /**
   * Gets the File associated with the FFmpeg Stream Logger file.
   *
   * @return the log file
   */
  public static Path getFFmpegStreamLogFile() {
    return FFMPEG_STREAM_LOG_FILE;
  }

  /**
   * Gets the File associated with the RTP Logger file.
   *
   * @return the log file
   */
  public static PrintWriter getRtpLoggerPath() {
    return RTP_LOGGER;
  }
}
