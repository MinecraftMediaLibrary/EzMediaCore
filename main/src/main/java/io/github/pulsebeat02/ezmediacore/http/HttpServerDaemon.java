package io.github.pulsebeat02.ezmediacore.http;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.http.request.ZipHeader;
import io.github.pulsebeat02.ezmediacore.http.request.ZipRequest;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;

public class HttpServerDaemon implements HttpDaemon, ZipRequest {

  private static final ExecutorService REQUEST_EXECUTOR;

  static {
    REQUEST_EXECUTOR = Executors.newCachedThreadPool();
  }

  private final Path directory;
  private final String ip;
  private final int port;
  private final boolean verbose;

  private ServerSocket socket;
  private ZipHeader header;
  private boolean running;

  public HttpServerDaemon(
      @NotNull final Path path, @NotNull final String ip, final int port, final boolean verbose)
      throws IOException {
    this.running = true;
    this.directory = path;
    this.ip = ip;
    this.port = port;
    this.verbose = verbose;
    this.header = ZipHeader.ZIP;

    try {
      this.socket = new ServerSocket(port);
      this.socket.setReuseAddress(true);
    } catch (final BindException e) {
      Logger.error(
          "The port specified is being used by another process. Please make sure to port-forward the port first and make sure it is open.");
      Logger.error(e.getMessage());
      return;
    }

    Logger.info("========================================");
    Logger.info("           Started HTTP Server:         ");
    Logger.info("========================================");
    Logger.info("IP Address: %s".formatted(ip));
    Logger.info("Port: %d".formatted(port));
    Logger.info("Directory: %s".formatted(path));
    Logger.info("========================================");
  }

  public HttpServerDaemon(
      @NotNull final MediaLibraryCore core,
      @NotNull final String ip,
      final int port,
      final boolean verbose)
      throws IOException {
    this(core.getHttpServerPath(), ip, port, verbose);
  }

  @Override
  public void start() {
    this.onServerStart();
    while (this.running) {
      try {
        REQUEST_EXECUTOR.submit(new FileRequestHandler(this, this.socket.accept(), this.header));
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
    this.running = false;
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

  @Override
  public void setZipHeader(@NotNull final ZipHeader header) {
    this.header = header;
  }
}
