package io.github.pulsebeat02.ezmediacore.http.request;

import java.net.Socket;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface Request extends Runnable {

  @NotNull
  String createHeader(@NotNull final Path file);

  @NotNull
  Socket getClient();

  void handleIncomingRequest();
}
