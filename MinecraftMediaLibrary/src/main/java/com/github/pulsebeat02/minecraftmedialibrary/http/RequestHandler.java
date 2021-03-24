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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class used to handle incoming requests. It checks if the current request being recieved is a
 * GET request and sends the correct response back to the client.
 */
public class RequestHandler implements Runnable, RequestHandlerBase {

  private static final Pattern MATCHER;

  static {
    MATCHER = Pattern.compile("GET /?(\\S*).*");
  }

  private final HttpDaemon daemon;
  private final HttpDaemon.ZipHeader header;
  private final Socket client;

  /**
   * Instantiates a new Request handler.
   *
   * @param daemon the daemon
   * @param header the header
   * @param client the client
   */
  public RequestHandler(
      @NotNull final HttpDaemon daemon,
      final HttpDaemon.ZipHeader header,
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
      verbose("Received request '" + request + "' from " + client.getInetAddress().toString());
      final Matcher get = requestPattern(request);
      if (get.matches()) {
        request = get.group(1);
        final File result = requestFileCallback(request);
        if (result == null) {
          flag = true;
          pout.println("HTTP/1.0 400 Bad Request");
        } else {
          verbose("Request '" + request + "' is being served to " + client.getInetAddress());
          try {
            out.write(buildHeader(result).getBytes(StandardCharsets.UTF_8));
            final FileInputStream fis = new FileInputStream(result);
            final byte[] data = new byte[64 * 1024];
            for (int read; (read = fis.read(data)) > -1; ) {
              out.write(data, 0, read);
            }
            out.flush();
            fis.close();
            verbose("Successfully served '" + request + "' to " + client.getInetAddress());
          } catch (final FileNotFoundException e) {
            flag = true;
            pout.println("HTTP/1.0 404 Object Not Found");
          }
        }
      } else {
        flag = true;
        pout.println("HTTP/1.0 400 Bad Request");
      }
      client.close();
    } catch (final IOException e) {
      flag = true;
      verbose("I/O error " + e);
    }
    if (flag) {
      daemon.onResourcepackFailedDownload(client);
    }
  }

  /**
   * Checks if the request matches the GET pattern.
   *
   * @param req request
   * @return request Matcher
   */
  private Matcher requestPattern(final String req) {
    return MATCHER.matcher(req);
  }

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
  public File requestFileCallback(final String request) {
    return new File(daemon.getParentDirectory(), request);
  }

  @Override
  public String buildHeader(final @NotNull File f) {
    return String.format(
        "HTTP/1.0 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\nDate: %s GMT\r\nServer: HttpDaemon\r\nUser-Agent: HTTPDaemon/1.0.0 (Resourcepack Hosting)\r\n\r\n",
        header.getHeader(),
        f.length(),
        new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
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
   * Gets header.
   *
   * @return the header
   */
  public HttpDaemon.ZipHeader getHeader() {
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
