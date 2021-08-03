package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.http.HttpDaemon;
import org.jetbrains.annotations.NotNull;

public interface HttpDaemonSolution extends LibraryInjectable, HostingSolution {

  void startServer();

  void stopServer();

  @NotNull
  HttpDaemon getDaemon();

  @Override
  @NotNull
  default String getName() {
    return "EzMediaCore HTTP Integrated Server";
  }
}
