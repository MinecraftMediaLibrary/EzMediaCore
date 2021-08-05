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
import java.net.ServerSocket;
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

  private Socket client;

  public FileRequestHandler(
      @NotNull final HttpServerDaemon daemon,
      @NotNull final ServerSocket client,
      final ZipHeader header) {
    this.daemon = daemon;
    try {
      this.client = client.accept();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.header = header;
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
