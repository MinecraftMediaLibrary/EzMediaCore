package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import com.wrapper.spotify.SpotifyApi;
import org.jetbrains.annotations.NotNull;

public final class SpotifyProvider {

  private static SpotifyApi SPOTIFY_API;

  public static void initialize(@NotNull final String accessToken) {
    new SpotifyApi.Builder().setAccessToken(accessToken).build();
  }

  protected static SpotifyApi getSpotifyApi() {
    return SPOTIFY_API;
  }
}
