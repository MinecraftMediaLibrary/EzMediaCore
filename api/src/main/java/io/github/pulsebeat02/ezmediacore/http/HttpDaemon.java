package io.github.pulsebeat02.ezmediacore.http;

import java.net.Socket;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface HttpDaemon {

  void start();

  void onServerStart();

  void stop();

  void onServerTermination();

  void onClientConnection(@NotNull final Socket client);

  void onRequestFailure(@NotNull final Socket client);

  boolean isVerbose();

  @NotNull
  Path getServerPath();

  int getPort();

  @NotNull
  String getAddress();

  @NotNull
  default Path getRelativePath(@NotNull final Path file) {
    return getServerPath().relativize(file);
  }
}
