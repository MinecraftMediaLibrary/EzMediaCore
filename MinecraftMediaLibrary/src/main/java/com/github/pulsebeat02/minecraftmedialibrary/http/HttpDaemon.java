/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.http;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpDaemon extends Thread implements AbstractHttpDaemon {

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
    this.running = true;
    this.port = port;
    this.socket = new ServerSocket(port);
    this.socket.setReuseAddress(true);
    this.directory = directory;
    this.header = ZipHeader.ZIP;
    this.verbose = true;
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
    this.running = true;
    this.port = port;
    try {
      this.socket = new ServerSocket(port);
      this.socket.setReuseAddress(true);
    } catch (final BindException e) {
      Logger.error(
          "The port specified is being used by another process. Please make sure to port-forward the port first and make sure it is open.");
      Logger.error(e.getMessage());
      return;
    }
    this.directory = new File(path);
    this.header = ZipHeader.ZIP;
    this.verbose = true;
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
    onServerStart();
    while (running) {
      try {
        new Thread(new RequestHandler(this, header, socket.accept())).start();
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
   * @param client
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
