/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.http;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.http.request.FileRequest;
import io.github.pulsebeat02.ezmediacore.http.request.ZipHeader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class FileRequestHandler implements FileRequest {

  private static final Pattern MATCHER;

  static {
    MATCHER = Pattern.compile("GET /?(\\S*).*");
  }

  private final HttpServerDaemon daemon;
  private final ZipHeader header;

  private final Socket client;

  public FileRequestHandler(
      @NotNull final HttpServerDaemon daemon,
      @NotNull final Socket client,
      final ZipHeader header) {
    this.daemon = daemon;
    this.client = client;
    this.header = header;
  }

  @Override
  public void run() {
    this.handleIncomingRequest();
  }

  @Override
  public @NotNull Path requestFileCallback(@NotNull final String request) {
    return this.daemon.getServerPath().resolve(request);
  }

  @Override
  public @NotNull ZipHeader getHeader() {
    return this.header;
  }

  @Override
  public @NotNull String createHeader(@NotNull final Path file) {
    try {
      return "HTTP/1.0 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\nDate: %s GMT\r\nServer: HttpDaemon\r\nUser-Agent: HTTPDaemon/1.0.0 (Resourcepack Hosting)\r\n\r\n"
          .formatted(
              this.header.getHeader(),
              Files.size(file),
              new SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                  .format(Calendar.getInstance().getTime()));
    } catch (final IOException e) {
      this.daemon.onRequestFailure(this.client);
      Logger.info(e.getMessage());
    }
    return "";
  }

  @Override
  public @NotNull Socket getClient() {
    return this.client;
  }

  @Override
  public void handleIncomingRequest() {
    this.daemon.onClientConnection(this.client);
    boolean flag = false;
    try (final BufferedReader in =
            new BufferedReader(new InputStreamReader(this.client.getInputStream(), "8859_1"));
        final OutputStream out = this.client.getOutputStream();
        final PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, "8859_1"), true)) {
      final InetAddress address = this.client.getInetAddress();
      String request = in.readLine();
      this.verbose("Received request '%s' from %s".formatted(request, address.toString()));
      final Matcher get = MATCHER.matcher(request);
      if (get.matches()) {
        request = get.group(1);
        final Path result = this.requestFileCallback(request);
        this.verbose("Request '%s' is being served to %s".formatted(request, address));
        try {
          out.write(this.createHeader(result).getBytes(StandardCharsets.UTF_8));
          try (final WritableByteChannel channel = Channels.newChannel(out)) {
            FileChannel.open(result).transferTo(0, Long.MAX_VALUE, channel);
          }
          this.verbose("Successfully served '%s' to %s".formatted(request, address));
        } catch (final FileNotFoundException e) {
          flag = true;
          pout.println("HTTP/1.0 404 Object Not Found");
        }
      } else {
        flag = true;
        pout.println("HTTP/1.0 400 Bad Request");
      }
      this.client.close();
    } catch (final IOException e) {
      flag = true;
      this.verbose("I/O error %s".formatted(e));
    }
    if (flag) {
      this.daemon.onRequestFailure(this.client);
    }
  }

  private void verbose(final String info) {
    if (this.daemon.isVerbose()) {
      Logger.info(info);
    }
  }
}
