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

package com.github.pulsebeat02.minecraftmedialibrary.playlist;

import com.github.pulsebeat02.minecraftmedialibrary.http.HttpFileDaemonServer;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Set;

/**
 * A class used to handle incoming requests. It checks the request and then appropriately changes
 * the player as so.
 */
public class PlayerRequestHandler implements PlayerRequestHandlerBase {

  private static final Set<String> VALID_CONTROLS;

  static {
    VALID_CONTROLS = ImmutableSet.of("PAUSE", "RESUME", "SKIP_FORWARD", "SKIP_BACWARD");
  }

  private final HttpFileDaemonServer daemon;
  private final WebResponsivePlayer audioPlayer;
  private final Socket client;

  /**
   * Instantiates a new request handler.
   *
   * @param daemon the daemon
   * @param audioPlayer the web audio player
   * @param client the client
   */
  public PlayerRequestHandler(
      @NotNull final HttpFileDaemonServer daemon,
      @NotNull final WebResponsivePlayer audioPlayer,
      @NotNull final Socket client) {
    this.daemon = daemon;
    this.audioPlayer = audioPlayer;
    this.client = client;
  }

  /** Runs the request handler. */
  @Override
  public void run() {
    handleRequest();
  }

  /** Handles the request once the client connects */
  @Override
  public void handleRequest() {
    daemon.onClientConnect(client);
    final boolean flag = false;
    try (final BufferedReader br =
        new BufferedReader(new InputStreamReader(client.getInputStream(), "8859_1"))) {
      final String request = br.readLine();
      verbose(
          String.format(
              "Received request '%s' from %s", request, client.getInetAddress().toString()));
      if (!VALID_CONTROLS.contains(request)) {
        client.close();
        return;
      }
      switch (request) {
        case "PAUSE":
          audioPlayer.pauseSong();
          break;
        case "RESUME":
          audioPlayer.resumeSong();
          break;
        case "SKIP_FORWARD":
          audioPlayer.skipForwardSong();
          break;
        case "SKIP_BACKWARD":
          audioPlayer.skipBackwardSong();
          break;
      }
      client.close();
    } catch (final IOException e) {
      daemon.onRequestFailed(client);
      verbose(String.format("I/O error %s", e));
    }
  }

  private void verbose(final String info) {
    if (daemon.isVerbose()) {
      Logger.info(info);
    }
  }

  @Override
  @NotNull
  public String buildHeader(final @NotNull Path f) {
    return "";
  }

  public HttpFileDaemonServer getDaemon() {
    return daemon;
  }

  @Override
  public Socket getClient() {
    return client;
  }
}
