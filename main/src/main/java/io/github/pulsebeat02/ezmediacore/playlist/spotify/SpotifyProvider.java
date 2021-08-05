package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.SpotifyApi;
import org.jetbrains.annotations.NotNull;

public final class SpotifyProvider {

  private static SpotifyApi SPOTIFY_API;

  public static void initialize(@NotNull final String accessToken) {
    SPOTIFY_API = new SpotifyApi.Builder().setAccessToken(accessToken).build();
  }

  static SpotifyApi getSpotifyApi() {
    return SPOTIFY_API;
  }
}
