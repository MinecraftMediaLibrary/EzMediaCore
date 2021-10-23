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

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.http.request.ZipHeader;
import io.github.pulsebeat02.ezmediacore.http.request.ZipRequest;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class HttpServerDaemon implements HttpDaemon, ZipRequest {

  private final Path directory;
  private final String ip;
  private final int port;
  private final boolean verbose;
  private final AtomicBoolean running;
  private final ZipHeader header;

  private ServerSocket socket;

  public HttpServerDaemon(
      @NotNull final Path path, @NotNull final String ip, final int port, final boolean verbose) {
    this.running = new AtomicBoolean(false);
    this.directory = path;
    this.ip = ip;
    this.port = port;
    this.verbose = verbose;
    this.header = ZipHeader.ZIP;
    this.logServerInformation();
  }

  public HttpServerDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    this(core.getHttpServerPath(), ip, port, verbose);
  }

  private void logServerInformation() {
    Logger.info("========================================");
    Logger.info("               HTTP Server:             ");
    Logger.info("========================================");
    Logger.info("IP Address: %s".formatted(this.ip));
    Logger.info("Port: %d".formatted(this.port));
    Logger.info("Directory: %s".formatted(this.directory));
    Logger.info("========================================");
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
      Logger.error(
          "The port specified is being used by another process. Please make sure to port-forward the port first and make sure it is open.");
      Logger.error(e.getMessage());
    }
  }

  private void handleServerRequests() {
    while (this.running.get()) {
      try {
        ExecutorProvider.HTTP_REQUEST_POOL.submit(
            new FileRequestHandler(this, this.socket.accept(), this.header));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onServerStart() {
  }

  @Override
  public void stop() {
    this.onServerTermination();
    this.running.set(false);
    if (!this.socket.isClosed()) {
      try {
        this.socket.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onServerTermination() {
  }

  @Override
  public void onClientConnection(@NotNull final Socket client) {
  }

  @Override
  public void onRequestFailure(@NotNull final Socket client) {
  }

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
}
