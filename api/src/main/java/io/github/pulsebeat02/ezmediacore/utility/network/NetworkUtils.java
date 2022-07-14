package io.github.pulsebeat02.ezmediacore.utility.network;

import java.io.IOException;
import java.net.ServerSocket;

public final class NetworkUtils {

  private NetworkUtils() {}

  public static int getFreePort() {
    try (final ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }
}
