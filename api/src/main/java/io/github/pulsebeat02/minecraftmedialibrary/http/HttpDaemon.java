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

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

/**
 * An interface to specify custom Http Daemon classes. Used within the MinecraftMediaLibrary as
 * well.
 */
public interface HttpDaemon {

  /** Method used to start the HTTP Daemon. */
  void startServer();

  /** Method used to stop the HTTP Daemon. */
  void stopServer();

  /** Called right before the HTTP Daemon starts running. */
  void onServerStart();

  /** Called right before the HTTP Daemon terminates. */
  void onServerTerminate();

  /**
   * Called when an incoming user connects to the HTTP Server.
   *
   * @param client for the incoming connection.
   */
  void onClientConnect(final Socket client);

  /**
   * Called when a resourcepack failed to be installed for a user.
   *
   * @param socket for the connection which failed download.
   */
  void onRequestFailed(final Socket socket);

  /**
   * Is verbose boolean.
   *
   * @return the boolean
   */
  boolean isVerbose();

  /**
   * Sets verbose.
   *
   * @param verbose the verbose
   */
  void setVerbose(final boolean verbose);

  /**
   * Gets parent directory.
   *
   * @return the parent directory
   */
  Path getParentDirectory();

  /**
   * Gets port.
   *
   * @return the port
   */
  int getPort();

  /**
   * Is running boolean.
   *
   * @return the boolean
   */
  boolean isRunning();

  /**
   * Gets socket.
   *
   * @return the socket
   */
  ServerSocket getSocket();

  /**
   * Gets directory.
   *
   * @return the directory
   */
  Path getDirectory();
}
