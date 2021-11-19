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
package io.github.pulsebeat02.ezmediacore.rtp;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RTPStreamingServer implements StreamingServer {

  private final MediaLibraryCore core;
  private final String ip;
  private final int hlsPort;
  private final AtomicBoolean cancelled;
  private Process process;

  RTPStreamingServer(
      @NotNull final MediaLibraryCore core, @NotNull final String ip, final int hlsPort) {
    this.core = core;
    this.ip = ip;
    this.hlsPort = hlsPort;
    this.cancelled = new AtomicBoolean(false);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull RTPStreamingServer ofRtpServer(@NotNull final MediaLibraryCore core) {
    return ofRtpServer(core, HttpServer.HTTP_SERVER_IP, 8080);
  }

  @Contract(value = "_, _ -> new", pure = true)
  public static @NotNull RTPStreamingServer ofRtpServer(
      @NotNull final MediaLibraryCore core, final int port) {
    return ofRtpServer(core, HttpServer.HTTP_SERVER_IP, port);
  }

  @Contract(value = "_, _ -> new", pure = true)
  public static @NotNull RTPStreamingServer ofRtpServer(
      @NotNull final MediaLibraryCore core, @NotNull final String ip) {
    return ofRtpServer(core, ip, 8080);
  }

  @Contract(value = "_, _, _ -> new", pure = true)
  public static @NotNull RTPStreamingServer ofRtpServer(
      @NotNull final MediaLibraryCore core, @NotNull final String ip, final int hlsPort) {
    return new RTPStreamingServer(core, ip, hlsPort);
  }

  @Override
  public void execute() {
    this.executeWithLogging(null);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    if (!this.cancelled.get()) {
      final ProcessBuilder builder =
          new ProcessBuilder(this.core.getRTPPath().toString()).redirectErrorStream(true);
      final Map<String, String> env = builder.environment();
      env.put("RTSP_HLSADDRESS", ":%s".formatted(this.hlsPort));
      try {
        this.process = builder.start();
        this.handleLogging(logger, logger != null);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void handleLogging(@Nullable final Consumer<String> logger, final boolean consume)
      throws IOException {
    try (final BufferedReader r =
        new BufferedReader(
            new InputStreamReader(new FastBufferedInputStream(this.process.getInputStream())))) {
      String line;
      while (true) {
        line = r.readLine();
        if (line == null) {
          break;
        }
        if (consume) {
          logger.accept(line);
        } else {
          this.log(line);
        }
      }
    }
  }

  @Override
  public void log(@NotNull final String line) {
    this.core.getLogger().rtp(line);
  }

  @Override
  public CompletableFuture<Void> executeAsync() {
    return CompletableFuture.runAsync(this::execute);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsync(@NotNull final Executor executor) {
    return CompletableFuture.runAsync(this::execute, executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger) {
    return CompletableFuture.runAsync(() -> this.executeWithLogging(logger));
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger, @NotNull final Executor executor) {
    return CompletableFuture.runAsync(() -> this.executeWithLogging(logger), executor);
  }

  @Override
  public void close() {
    this.cancelled.set(true);
    if (this.process != null) {
      this.process.destroyForcibly();
    }
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled.get();
  }

  @Override
  public @NotNull String getAddress() {
    return this.ip;
  }
}
