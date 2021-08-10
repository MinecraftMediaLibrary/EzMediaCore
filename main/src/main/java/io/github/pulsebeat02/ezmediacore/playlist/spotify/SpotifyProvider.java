package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.SpotifyApi;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.NotNull;

public final class SpotifyProvider {

  private static SpotifyApi SPOTIFY_API;

  private SpotifyProvider() {
  }

  public static void initialize(@NotNull final MediaLibraryCore core) {
    final SpotifyClient client = core.getSpotifyClient();
    if (client != null) {
      SPOTIFY_API = new SpotifyApi.Builder().setClientId(client.getClientID())
          .setClientSecret(client.getClientSecret()).build();
    }
  }

  static SpotifyApi getSpotifyApi() {
    return SPOTIFY_API;
  }
}
