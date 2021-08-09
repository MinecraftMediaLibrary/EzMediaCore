package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.SpotifyApi;
import org.jetbrains.annotations.NotNull;

public final class SpotifyProvider {

  private static SpotifyApi SPOTIFY_API;

  private SpotifyProvider() {
  }

  public static void initialize(@NotNull final SpotifyClient client) {
    SPOTIFY_API = new SpotifyApi.Builder().setClientId(client.getClientID())
        .setClientSecret(client.getClientSecret()).build();
  }

  static SpotifyApi getSpotifyApi() {
    return SPOTIFY_API;
  }
}
