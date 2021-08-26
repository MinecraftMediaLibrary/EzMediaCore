package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface HttpDaemonSolution extends HostingSolution {

  void startServer();

  void stopServer();

  @NotNull
  HttpDaemon getDaemon();

  @NotNull
  String createUrl(@NotNull final Path path);

  @Override
  @NotNull
  default String getName() {
    return "EzMediaCore HTTP Integrated Server";
  }
}
