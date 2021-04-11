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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting;

import com.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemon;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The wrapper class and provider for the Http daemon. Useful for creating http daemons and easier
 * handling.
 */
public class HttpDaemonProvider implements HostingProvider {

  private final String serverIP;
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
    serverIP = getPublicIP();
    try {
      daemon = new HttpDaemon(port, path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Instantiates a new Http daemon provider.
   *
   * @param path the path
   * @param port the port
   * @param ip the ip address
   */
  public HttpDaemonProvider(@NotNull final String path, final int port, @NotNull final String ip) {
    this.port = port;
    serverIP = ip;
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
  public String getServerIp() {
    return serverIP;
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
    return "https://" + serverIP + ":" + port + "/" + getRelativePath(file);
  }

  /**
   * Generates the URL based on file (Path)
   *
   * @param path to gnerate parent directory of the HTTP Server for.
   * @return file url
   */
  @Override
  public String generateUrl(@NotNull final Path path) {
    return "https://" + serverIP + ":" + port + "/" + getRelativePath(path.toString());
  }

  /**
   * Gets the external IP associated with the server. May be overridden with custom ip if necessary.
   *
   * @return the public ip
   */
  public String getPublicIP() {
    try (final BufferedReader in =
        new BufferedReader(
            new InputStreamReader(new URL("https://checkip.amazonaws.com").openStream()))) {
      final String line = in.readLine();
      in.close();
      return line;
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Gets relative path for file based off of HTTP server folder.
   *
   * @param absolutePath the absolute file path
   * @return the relative path when comparing to the HTTP server folder
   */
  public String getRelativePath(@NotNull final String absolutePath) {
    return Paths.get(daemon.getParentDirectory().getAbsolutePath())
        .relativize(Paths.get(absolutePath))
        .toString();
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
