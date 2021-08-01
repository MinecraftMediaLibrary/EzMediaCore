package io.github.pulsebeat02.epicmedialib.http;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.epicmedialib.Logger;
import io.github.pulsebeat02.epicmedialib.http.request.ZipHeader;
import io.github.pulsebeat02.epicmedialib.http.request.ZipRequest;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class HttpServerDaemon implements HttpDaemon, ZipRequest {

  private static final ExecutorService EXECUTOR_SERVICE;

  static {
    EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  }

  private final Path directory;
  private final String ip;
  private final int port;
  private final boolean verbose;

  private ServerSocket socket;
  private ZipHeader header;
  private boolean running;

  public HttpServerDaemon(
      @NotNull final String path, @NotNull final String ip, final int port, final boolean verbose)
      throws IOException {

    this.running = true;
    this.directory = Paths.get(path);
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
    Logger.info(String.format("IP Address: %s", ip));
    Logger.info(String.format("Port: %d", port));
    Logger.info(String.format("Directory: %s", path));
    Logger.info("========================================");
  }

  @Override
  public void start() {
    onServerStart();
    Preconditions.checkState(!Bukkit.isPrimaryThread());
    while (this.running) {
      EXECUTOR_SERVICE.submit(
          () -> new FileRequestHandler(this, this.socket, this.header).handleIncomingRequest());
    }
  }

  @Override
  public void onServerStart() {}

  @Override
  public void stop() {
    onServerTermination();
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
  public void setZipHeader(@NotNull final ZipHeader header) {
    this.header = header;
  }
}
