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
package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
import io.github.pulsebeat02.ezmediacore.http.netty.NettyServer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpServer implements HttpDaemonSolution {

  public static String HTTP_SERVER_IP;

  static {
    try {
      HTTP_SERVER_IP = HttpClient.newHttpClient().send(createRequest(), createBodyHandler()).body();
    } catch (final IOException | InterruptedException e) {
      HTTP_SERVER_IP = "127.0.0.1"; // fallback ip
    }
  }

  private final HttpDaemon daemon;
  private boolean running;

  HttpServer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @Nullable final String ip,
      final int port,
      final boolean verbose) {
    final String address = ip == null ? HTTP_SERVER_IP : ip;
    this.daemon = NettyServer.ofDaemon(core, path, address, port, verbose);
  }

  private static @NotNull HttpRequest createRequest() {
    return HttpRequest.newBuilder().uri(URI.create("https://myexternalip.com/raw")).build();
  }

  private static @NotNull BodyHandler<String> createBodyHandler() {
    return HttpResponse.BodyHandlers.ofString();
  }

  @Contract("_, _ -> new")
  public static @NotNull HttpServer ofServer(@NotNull final MediaLibraryCore core, final int port)
      throws IOException {
    return ofServer(core, port, true);
  }

  @Contract("_, _, _ -> new")
  public static @NotNull HttpServer ofServer(
      @NotNull final MediaLibraryCore core, final int port, final boolean verbose)
      throws IOException {
    return ofServer(core, HTTP_SERVER_IP, port, verbose);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull HttpServer ofServer(
      @NotNull final MediaLibraryCore core,
      @NotNull final String ip,
      final int port,
      final boolean verbose)
      throws IOException {
    return new HttpServer(core, core.getHttpServerPath(), ip, port, verbose);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull HttpServer ofServer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      final int port,
      final boolean verbose) {
    return ofServer(core, path, HTTP_SERVER_IP, port, verbose);
  }

  @Contract("_, _, _, _, _ -> new")
  public static @NotNull HttpServer ofServer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path path,
      @NotNull final String ip,
      final int port,
      final boolean verbose) {
    return new HttpServer(core, path, ip, port, verbose);
  }

  @Contract("_, _, _ -> new")
  public static @NotNull HttpServer ofServer(
      @NotNull final MediaLibraryCore core, @NotNull final Path path, final int port) {
    return ofServer(core, path, HTTP_SERVER_IP, port, true);
  }

  @Override
  public @NotNull String createUrl(@NotNull final String file) {
    return "http://%s:%d/%s"
        .formatted(
            this.daemon.getAddress(),
            this.daemon.getPort(),
            this.daemon.getRelativePath(Path.of(file)));
  }

  @Override
  public @NotNull String createUrl(@NotNull final Path path) {
    return this.createUrl(path.toString());
  }

  @Override
  public void startServer() {
    CompletableFuture.runAsync(this.daemon::start, ExecutorProvider.HTTP_SERVER);
    this.running = true;
  }

  @Override
  public void stopServer() {
    CompletableFuture.runAsync(this.daemon::stop,  ExecutorProvider.HTTP_SERVER);
    this.running = false;
  }

  @Override
  public @NotNull HttpDaemon getDaemon() {
    return this.daemon;
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }
}
