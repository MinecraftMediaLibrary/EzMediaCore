package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
import io.github.pulsebeat02.ezmediacore.http.HttpServerDaemon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class HttpServer implements HttpDaemonSolution {

  private static String HTTP_SERVER_IP;

  static {
    try (final BufferedReader in =
        new BufferedReader(
            new InputStreamReader(new URL("https://checkip.amazonaws.com").openStream()))) {
      HTTP_SERVER_IP = in.readLine();
    } catch (final IOException e) {
      HTTP_SERVER_IP = "127.0.0.1"; // fallback ip
      e.printStackTrace();
    }
  }

  private final HttpDaemon daemon;

  public HttpServer(@NotNull final String path, final int port) throws IOException {
    this(path, HTTP_SERVER_IP, port, true);
  }

  public HttpServer(@NotNull final String path, final int port, final boolean verbose)
      throws IOException {
    this(path, HTTP_SERVER_IP, port, verbose);
  }

  public HttpServer(
      @NotNull final String path, @NotNull final String ip, final int port, final boolean verbose)
      throws IOException {
    this.daemon = new HttpServerDaemon(path, ip, port, verbose);
  }

  @Override
  public @NotNull String createUrl(@NotNull final Path file) {
    return String.format(
        "http://%s:%d/%s",
        daemon.getAddress(), daemon.getPort(), this.daemon.getRelativePath(file));
  }

  @Override
  public void startServer() {
    this.daemon.start();
  }

  @Override
  public void stopServer() {
    this.daemon.stop();
  }

  @Override
  public @NotNull HttpDaemon getDaemon() {
    return this.daemon;
  }
}
