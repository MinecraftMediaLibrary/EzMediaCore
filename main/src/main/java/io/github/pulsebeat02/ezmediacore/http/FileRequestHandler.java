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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import io.github.pulsebeat02.ezmediacore.http.request.FileRequest;
import io.github.pulsebeat02.ezmediacore.http.request.RequestHeaderArguments;
import io.github.pulsebeat02.ezmediacore.http.request.ZipHeader;
import io.github.pulsebeat02.ezmediacore.utility.http.HttpUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
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
import java.util.List;
import java.util.Set;
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
    checkNotNull(daemon, "Daemon cannot be null!");
    checkNotNull(client, "Client cannot be null!");
    checkNotNull(header, "Header cannot be null!");
    this.daemon = daemon;
    this.client = client;
    this.header = header;
  }

  @Override
  public void run() {
    this.handleIncomingRequest();
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
      throw new AssertionError(e);
    }
  }

  private @NotNull String produceHeader(@NotNull final Path file) throws IOException {

    final List<String> arguments = Lists.newArrayList();
    arguments.add(RequestHeaderArguments.HTTP_HEADER);
    arguments.add(System.lineSeparator());

    final String header = this.header.getHeader();
    arguments.addAll(Set.of(RequestHeaderArguments.CONTENT_TYPE, header));
    arguments.add(System.lineSeparator());

    final String size = String.valueOf(Files.size(file));
    arguments.addAll(Set.of(RequestHeaderArguments.CONTENT_LENGTH, size));
    arguments.add(System.lineSeparator());

    final String date = DATE_FORMAT.format(Calendar.getInstance().getTime());
    arguments.addAll(Set.of(RequestHeaderArguments.DATE, "%s GMT".formatted(date)));
    arguments.add(System.lineSeparator());

    arguments.addAll(Set.of(RequestHeaderArguments.SERVER, "HttpDaemon"));
    arguments.add(System.lineSeparator());

    arguments.addAll(Set.of(RequestHeaderArguments.USER_AGENT, "HTTPDaemon/1.0.0 (Resourcepack Hosting)"));
    arguments.add(System.lineSeparator());
    arguments.add(System.lineSeparator());

    return String.join("", arguments);
  }

  @Override
  public @NotNull Socket getClient() {
    return this.client;
  }

  @Override
  public void handleIncomingRequest() {

    this.daemon.onClientConnection(this.client);

    boolean flag;
    try (final BufferedReader in = this.createFastBufferedReader();
        final OutputStream out = this.client.getOutputStream();
        final PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, "8859_1"), true)) {
      flag = this.handleRequest(in, out, pout);
      Try.closeable(this.client);
    } catch (final IOException e) {
      flag = true;
      e.printStackTrace();
    }

    if (flag) {
      this.daemon.onRequestFailure(this.client);
    }
  }

  private boolean handleRequest(
      final @NotNull BufferedReader in,
      @NotNull final OutputStream out,
      @NotNull final PrintWriter pout)
      throws IOException {
    final InetAddress address = this.client.getInetAddress();
    return !this.handleRequest(address, in.readLine(), pout, out);
  }

  private @NotNull BufferedReader createFastBufferedReader() throws IOException {
    return new BufferedReader(
        new InputStreamReader(new FastBufferedInputStream(this.client.getInputStream()), "8859_1"));
  }

  private boolean handleRequest(
      @NotNull final InetAddress address,
      @NotNull final String request,
      @NotNull final PrintWriter pout,
      @NotNull final OutputStream out)
      throws IOException {

    this.verbose("Received request '%s' from %s".formatted(request, address.toString()));

    final Matcher get = GET_REQUEST.matcher(request);

    if (!get.matches()) {
      pout.println(INVALID_PATH);
      return false;
    }

    if (!this.handleGetRequest(address, out, get)) {
      pout.println(INVALID_PATH);
      return false;
    }

    return true;
  }

  private boolean handleGetRequest(
      @NotNull final InetAddress address,
      @NotNull final OutputStream out,
      @NotNull final Matcher get)
      throws IOException {

    final String group = get.group(1);
    if (HttpUtils.checkTreeAttack(group)) {
      return false;
    }

    this.sendFile(address, out, group);

    return true;
  }

  private void sendFile(
      @NotNull final InetAddress address, @NotNull final OutputStream out, final String group)
      throws IOException {
    final Path result = this.requestFileCallback(group);
    out.write(this.createHeader(result));
    this.sendFile(result, address, out, group);
  }

  @Override
  public @NotNull Path requestFileCallback(@NotNull final String request) {
    return this.daemon.getServerPath().resolve(request);
  }

  private void sendFile(
      @NotNull final Path result,
      @NotNull final InetAddress address,
      @NotNull final OutputStream out,
      @NotNull final String group)
      throws IOException {
    this.verbose("Request '%s' is being served to %s".formatted(group, address));
    this.openWritableChannel(result, out);
    this.verbose("Successfully served '%s' to %s".formatted(group, address));
  }

  private void openWritableChannel(@NotNull final Path result, @NotNull final OutputStream out)
      throws IOException {
    try (final WritableByteChannel channel = Channels.newChannel(out)) {
      FileChannel.open(result).transferTo(0, Long.MAX_VALUE, channel);
    }
  }

  private void verbose(final String info) {
    if (this.daemon.isVerbose()) {
      this.daemon.getCore().getLogger().info(info);
    }
  }
}
