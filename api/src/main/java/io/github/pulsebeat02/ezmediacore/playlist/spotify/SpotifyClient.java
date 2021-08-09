package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import org.jetbrains.annotations.NotNull;

public final class SpotifyClient {

  private final String clientID;
  private final String clientSecret;

  public SpotifyClient(@NotNull final String clientID, @NotNull final String clientSecret) {
    this.clientID = clientID;
    this.clientSecret = clientSecret;
  }

  public String getClientID() {
    return this.clientID;
  }

  public String getClientSecret() {
    return this.clientSecret;
  }
}
