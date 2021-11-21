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
package io.github.pulsebeat02.ezmediacore.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.http.request.ZipHeader;
import io.github.pulsebeat02.ezmediacore.http.request.ZipRequest;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HttpServerDaemon implements HttpDaemon, ZipRequest {

  private static final ExecutorService HTTP_REQUEST_POOL;

  static {
    HTTP_REQUEST_POOL = Executors.newCachedThreadPool();
  }

  private final MediaLibraryCore core;
  private final Path directory;
  private final String ip;
  private final int port;
  private final boolean verbose;
  private final AtomicBoolean running;
  private final ZipHeader header;
  private final ExecutorService executor;

  private ServerSocket socket;

  HttpServerDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    this.core = core;
    this.executor = Executors.newSingleThreadExecutor();
    this.running = new AtomicBoolean(false);
    this.directory = path;
    this.ip = ip;
    this.port = port;
    this.verbose = verbose;
    this.header = ZipHeader.ZIP;
    this.logServerInformation();
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull HttpServerDaemon ofDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    return ofDaemon(core, core.getHttpServerPath(), ip, port, verbose);
  }

  @Contract("_, _, _, _, _ -> new")
  public static @NotNull HttpServerDaemon ofDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    return new HttpServerDaemon(core, path, ip, port, verbose);
  }

  private void logServerInformation() {
    this.core.getLogger().info(Locale.HTTP_INFO.build(this.ip, this.port, this.directory));
  }

  @Override
  public void start() {
    this.onServerStart();
    this.openServerSocket();
    this.handleServerRequests();
  }

  private void openServerSocket() {
    this.running.set(true);
    try {
      this.socket = new ServerSocket(this.port);
      this.socket.setReuseAddress(true);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void handleServerRequests() {
    CompletableFuture.runAsync(
        () -> {
          try {
            while (this.running.get()) {
              CompletableFuture.runAsync(
                  new FileRequestHandler(this, this.socket.accept(), this.header),
                  HTTP_REQUEST_POOL);
            }
          } catch (final IOException e) {
            e.printStackTrace();
          }
        },
        this.executor);
  }

  @Override
  public void onServerStart() {}

  @Override
  public void stop() {
    try {
      this.onServerTermination();
      this.running.set(false);
      if (!this.socket.isClosed()) {
        this.socket.close();
      }
      this.executor.shutdown();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onServerTermination() {}

  @Override
  public void onClientConnection(@NotNull final Socket client) {}

  @Override
  public void onRequestFailure(@NotNull final Socket client) {}

  @Override
  public boolean isVerbose() {
    return this.verbose;
  }

  @Override
  public @NotNull Path getServerPath() {
    return this.directory;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public @NotNull String getAddress() {
    return this.ip;
  }

  @Override
  public @NotNull ZipHeader getHeader() {
    return this.header;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
