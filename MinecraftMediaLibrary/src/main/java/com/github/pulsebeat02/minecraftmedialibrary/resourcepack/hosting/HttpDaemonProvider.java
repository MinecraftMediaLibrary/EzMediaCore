/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting;

import com.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemon;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The wrapper class and provider for the Http daemon. Useful for creating http daemons and easier
 * handling.
 */
public class HttpDaemonProvider implements HostingProvider {

  private static final String SERVER_IP = Bukkit.getIp();
  private final int port;
  private HttpDaemon daemon;

  /**
   * Instantiates a new Http daemon provider.
   *
   * @param path the path
   * @param port the port
   */
  public HttpDaemonProvider(@NotNull final String path, final int port) {
    this.port = port;
    try {
      daemon = new HttpDaemon(port, path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets server ip.
   *
   * @return the server ip
   */
  public static String getServerIp() {
    return SERVER_IP;
  }

  /** Start server. */
  public void startServer() {
    daemon.start();
  }

  /**
   * Generates the URL based on file (String)
   *
   * @param file to generate parent directory of the HTTP Server for.
   * @return file url
   */
  @Override
  public String generateUrl(@NotNull final String file) {
    return "http://" + SERVER_IP + ":" + port + "/" + file;
  }

  /**
   * Generates the URL based on file (Path)
   *
   * @param path to gnerate parent directory of the HTTP Server for.
   * @return file url
   */
  @Override
  public String generateUrl(@NotNull final Path path) {
    return "http://" + SERVER_IP + ":" + port + "/" + path.getFileName();
  }

  /**
   * Gets daemon.
   *
   * @return the daemon
   */
  public HttpDaemon getDaemon() {
    return daemon;
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  public int getPort() {
    return port;
  }
}
