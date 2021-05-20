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
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class used to handle incoming requests. It checks if the current request being received is a
 * GET request and sends the correct response back to the client.
 */
public class FileRequestHandler implements Runnable, RequestHandler {

  private static final Pattern MATCHER;

  static {
    MATCHER = Pattern.compile("GET /?(\\S*).*");
  }

  private final HttpFileDaemonServer daemon;
  private final HttpFileDaemonServer.ZipHeader header;
  private final Socket client;

  /**
   * Instantiates a new Request handler.
   *
   * @param daemon the daemon
   * @param header the header
   * @param client the client
   */
  public FileRequestHandler(
      @NotNull final HttpFileDaemonServer daemon,
      final HttpFileDaemonServer.ZipHeader header,
      @NotNull final Socket client) {
    this.daemon = daemon;
    this.header = header;
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
    boolean flag = false;
    try {
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(client.getInputStream(), "8859_1"));
      final OutputStream out = client.getOutputStream();
      final PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, "8859_1"), true);
      String request = in.readLine();
      verbose(
          String.format(
              "Received request '%s' from %s", request, client.getInetAddress().toString()));
      final Matcher get = requestPattern(request);
      if (get.matches()) {
        request = get.group(1);
        final Path result = requestFileCallback(request);
        verbose(
            String.format("Request '%s' is being served to %s", request, client.getInetAddress()));
        try {
          out.write(buildHeader(result).getBytes(StandardCharsets.UTF_8));
          final FileInputStream fis = new FileInputStream(result.toFile());
          final byte[] data = new byte[64 * 1024];
          for (int read; (read = fis.read(data)) > -1; ) {
            out.write(data, 0, read);
          }
          out.flush();
          fis.close();
          verbose(
              String.format("Successfully served '%s' to %s", request, client.getInetAddress()));
        } catch (final FileNotFoundException e) {
          flag = true;
          pout.println("HTTP/1.0 404 Object Not Found");
        }
      } else {
        flag = true;
        pout.println("HTTP/1.0 400 Bad Request");
      }
      client.close();
    } catch (final IOException e) {
      flag = true;
      verbose(String.format("I/O error %s", e));
    }
    if (flag) {
      daemon.onRequestFailed(client);
    }
  }

  /**
   * Checks if the request matches the GET pattern.
   *
   * @param req request
   * @return request Matcher
   */
  @NotNull
  private Matcher requestPattern(final String req) {
    return MATCHER.matcher(req);
  }

  /**
   * If it is verbose, then it will print the information.
   *
   * @param info the info
   */
  private void verbose(final String info) {
    if (daemon.isVerbose()) {
      Logger.info(info);
    }
  }

  /**
   * Request file callback file.
   *
   * @param request the request
   * @return the file
   */
  @NotNull
  public Path requestFileCallback(final String request) {
    return daemon.getParentDirectory().resolve(request);
  }

  @Override
  @NotNull
  public String buildHeader(final @NotNull Path f) {
    try {
      return String.format(
          "HTTP/1.0 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\nDate: %s GMT\r\nServer: HttpDaemon\r\nUser-Agent: HTTPDaemon/1.0.0 (Resourcepack Hosting)\r\n\r\n",
          header.getHeader(),
          Files.size(f),
          new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Gets daemon.
   *
   * @return the daemon
   */
  public HttpFileDaemonServer getDaemon() {
    return daemon;
  }

  /**
   * Gets header.
   *
   * @return the header
   */
  public HttpFileDaemonServer.ZipHeader getHeader() {
    return header;
  }

  /**
   * Gets client.
   *
   * @return the client
   */
  public Socket getClient() {
    return client;
  }
}
