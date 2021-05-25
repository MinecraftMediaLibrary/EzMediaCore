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

package io.github.pulsebeat02.minecraftmedialibrary.http;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.minecraftmedialibrary.http.request.ZipHeader;
import io.github.pulsebeat02.minecraftmedialibrary.http.request.ZipRequest;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main daemon used for hosting resourcepack files. Other files may be hosted as well, however,
 * the purpose is mainly to host the packs to serve sound to users. A port that is port-forwarded
 * must be specified as well as a base directory. It then runs on an async thread while logging out
 * connections (if set to enabled).
 */
public class HttpFileDaemonServer extends Thread implements HttpDaemon, ZipRequest {

  private static final ExecutorService EXECUTOR_SERVICE;

  static {
    EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  }

  private final int port;
  private ServerSocket socket;
  private Path directory;
  private boolean running;
  private ZipHeader header;
  private boolean verbose;

  /**
   * Instantiates a new Http daemon.
   *
   * @param port the port
   * @param directory the directory
   * @throws IOException the io exception
   */
  public HttpFileDaemonServer(final int port, @NotNull final Path directory) throws IOException {
    this(port, directory.toAbsolutePath().toString());
  }

  /**
   * Instantiates a new HttpDaemon.
   *
   * @param port the port
   * @param path the path
   * @throws IOException the io exception
   */
  public HttpFileDaemonServer(final int port, @NotNull final String path) throws IOException {
    running = true;
    this.port = port;
    try {
      socket = new ServerSocket(port);
      socket.setReuseAddress(true);
    } catch (final BindException e) {
      Logger.error(
          "The port specified is being used by another process. Please make sure to port-forward the port first and make sure it is open.");
      Logger.error(e.getMessage());
      return;
    }
    directory = Paths.get(path);
    header = ZipHeader.ZIP;
    verbose = true;
    Logger.info("Started HTTP Server: ");
    Logger.info("========================================");
    Logger.info(String.format("IP: %s", Bukkit.getIp()));
    Logger.info(String.format("PORT: %d", port));
    Logger.info(String.format("DIRECTORY: %s", path));
    Logger.info("========================================");
  }

  /** Runs the HTTP Server. */
  @Override
  public void run() {
    startServer();
  }

  /** Server start method (called after event is called). */
  @Override
  public void startServer() {
    Preconditions.checkState(!Bukkit.isPrimaryThread());
    onServerStart();
    while (running) {
      try {
        EXECUTOR_SERVICE.submit(new FileRequestHandler(this, header, socket.accept()));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Terminate the Server. */
  @Override
  public void stopServer() {
    onServerTerminate();
    Logger.info(String.format("Terminating HTTP Server at %s:%d", Bukkit.getIp(), port));
    running = false;
    if (!socket.isClosed()) {
      try {
        socket.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onServerStart() {}

  @Override
  public void onServerTerminate() {}

  @Override
  public void onClientConnect(final Socket client) {}

  @Override
  public void onRequestFailed(final Socket client) {}

  @Override
  public ZipHeader getZipHeader() {
    return header;
  }

  @Override
  public void setZipHeader(@NotNull final ZipHeader header) {
    this.header = header;
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  @Override
  public Path getParentDirectory() {
    return directory;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public ServerSocket getSocket() {
    return socket;
  }

  @Override
  public Path getDirectory() {
    return directory;
  }
}
