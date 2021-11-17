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

  private static final Pattern GET_REQUEST;
  private static final SimpleDateFormat DATE_FORMAT;
  private static final String INVALID_PATH;

  static {
    GET_REQUEST = Pattern.compile("GET /?(\\S*).*");
    DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
    INVALID_PATH = "HTTP/1.0 400 Bad Request";
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
  public byte @NotNull [] createHeader(@NotNull final Path file) {
    try {
      return this.produceHeader(file).getBytes(StandardCharsets.UTF_8);
    } catch (final IOException e) {
      this.daemon.onRequestFailure(this.client);
      Logger.info(e.getMessage());
    }
    return new byte[] {};
  }

  private @NotNull String produceHeader(@NotNull final Path file) throws IOException {
    final long size = Files.size(file);
    final String format = DATE_FORMAT.format(Calendar.getInstance().getTime());
    return "HTTP/1.0 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\nDate: %s GMT\r\nServer: HttpDaemon\r\nUser-Agent: HTTPDaemon/1.0.0 (Resourcepack Hosting)\r\n\r\n"
        .formatted(this.header.getHeader(), size, format);
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
      if (!this.handleRequest(address, in.readLine(), pout, out)) {
        flag = true;
      }
      this.client.close();
    } catch (final IOException e) {
      flag = true;
      e.printStackTrace();
    }
    if (flag) {
      this.daemon.onRequestFailure(this.client);
    }
  }

  private boolean handleRequest(
      @NotNull final InetAddress address,
      @NotNull final String request,
      @NotNull final PrintWriter pout,
      @NotNull final OutputStream out)
      throws IOException {
    this.verbose("Received request '%s' from %s".formatted(request, address.toString()));
    final Matcher get = GET_REQUEST.matcher(request);
    if (get.matches()) {
      if (!this.handleGetRequest(address, out, pout, get)) {
        pout.println(INVALID_PATH);
      }
    } else {
      pout.println(INVALID_PATH);
      return false;
    }
    return true;
  }

  private boolean handleGetRequest(
      @NotNull final InetAddress address,
      @NotNull final OutputStream out,
      @NotNull final PrintWriter writer,
      @NotNull final Matcher get)
      throws IOException {
    final String group = get.group(1);
    if (this.checkTreeAttack(group)) {
      return false;
    }
    final Path result = this.requestFileCallback(group);
    out.write(this.createHeader(result));
    this.sendFile(result, address, out, group);
    return true;
  }

  private void sendFile(
      @NotNull final Path result,
      @NotNull final InetAddress address,
      @NotNull final OutputStream out,
      @NotNull final String group)
      throws IOException {
    this.verbose("Request '%s' is being served to %s".formatted(group, address));
    try (final WritableByteChannel channel = Channels.newChannel(out)) {
      FileChannel.open(result).transferTo(0, Long.MAX_VALUE, channel);
    }
    this.verbose("Successfully served '%s' to %s".formatted(group, address));
  }

  private boolean checkTreeAttack(@NotNull final String result) {
    return result.startsWith("..") || result.endsWith("..") || result.contains("../");
  }

  private void verbose(final String info) {
    if (this.daemon.isVerbose()) {
      Logger.info(info);
    }
  }
}
