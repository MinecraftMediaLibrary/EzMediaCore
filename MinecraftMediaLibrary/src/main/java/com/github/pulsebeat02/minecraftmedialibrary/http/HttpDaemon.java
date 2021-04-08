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

package com.github.pulsebeat02.minecraftmedialibrary.http;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main daemon used for hosting resourcepack files. Other files may be hosted as well, however,
 * the purpose is mainly to host the packs to serve sound to users. A port that is port-forwarded
 * must be specified as well as a base directory. It then runs on an async thread while logging out
 * connections (if set to enabled).
 */
public class HttpDaemon extends Thread implements HttpDaemonBase {

  private static final ExecutorService EXECUTOR_SERVICE;

  static {
    EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  }

  private final int port;
  private ServerSocket socket;
  private File directory;
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
  public HttpDaemon(final int port, @NotNull final File directory) throws IOException {
    running = true;
    this.port = port;
    socket = new ServerSocket(port);
    socket.setReuseAddress(true);
    this.directory = directory;
    header = ZipHeader.ZIP;
    verbose = true;
    Logger.info("Started HTTP Server: ");
    Logger.info("========================================");
    Logger.info("IP: " + Bukkit.getIp());
    Logger.info("PORT: " + port);
    Logger.info("DIRECTORY: " + directory.getAbsolutePath());
    Logger.info("========================================");
  }

  /**
   * Instantiates a new HttpDaemon.
   *
   * @param port the port
   * @param path the path
   * @throws IOException the io exception
   */
  public HttpDaemon(final int port, @NotNull final String path) throws IOException {
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
    directory = new File(path);
    header = ZipHeader.ZIP;
    verbose = true;
    Logger.info("Started HTTP Server: ");
    Logger.info("========================================");
    Logger.info("IP: " + Bukkit.getIp());
    Logger.info("PORT: " + port);
    Logger.info("DIRECTORY: " + path);
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
        EXECUTOR_SERVICE.submit(new Thread(new RequestHandler(this, header, socket.accept())));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Terminate the Server. */
  public void terminate() {
    onServerTerminate();
    Logger.info("Terminating HTTP Server at " + Bukkit.getIp() + ":" + port);
    running = false;
    if (!socket.isClosed()) {
      try {
        socket.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Called right when the server starts. */
  @Override
  public void onServerStart() {}

  /** Called when the server is being terminated. */
  @Override
  public void onServerTerminate() {}

  /**
   * Called if an incoming client is connecting
   *
   * @param client for the incoming connection.
   */
  @Override
  public void onClientConnect(final Socket client) {}

  /**
   * Called if a resourcepack failed to download for a user.
   *
   * @param client client
   */
  @Override
  public void onResourcepackFailedDownload(final Socket client) {}

  /**
   * Gets zip header.
   *
   * @return the zip header
   */
  public ZipHeader getZipHeader() {
    return header;
  }

  /**
   * Sets zip header.
   *
   * @param header the header
   */
  public void setZipHeader(final ZipHeader header) {
    this.header = header;
  }

  /**
   * Is verbose boolean.
   *
   * @return the boolean
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Sets verbose.
   *
   * @param verbose the verbose
   */
  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Gets parent directory.
   *
   * @return the parent directory
   */
  public File getParentDirectory() {
    return directory;
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Is running boolean.
   *
   * @return the boolean
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Gets socket.
   *
   * @return the socket
   */
  public ServerSocket getSocket() {
    return socket;
  }

  /**
   * Gets directory.
   *
   * @return the directory
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Gets header.
   *
   * @return the header
   */
  public ZipHeader getHeader() {
    return header;
  }

  /** The enum Zip header. */
  public enum ZipHeader {

    /** ZIP Header */
    ZIP("application/zip"),

    /** Octet Stream Header */
    OCTET_STREAM("application/octet-stream");

    private final String header;

    ZipHeader(final String header) {
      this.header = header;
    }

    /**
     * Gets header.
     *
     * @return the header
     */
    public String getHeader() {
      return header;
    }
  }
}
