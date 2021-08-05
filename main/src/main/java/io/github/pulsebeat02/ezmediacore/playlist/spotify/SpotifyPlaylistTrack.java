package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import java.util.Date;
import org.jetbrains.annotations.NotNull;

public record SpotifyPlaylistTrack(
        com.wrapper.spotify.model_objects.specification.PlaylistTrack track) implements PlaylistTrack {

  public SpotifyPlaylistTrack(
          @NotNull final com.wrapper.spotify.model_objects.specification.PlaylistTrack track) {
    this.track = track;
  }

  @Override
  public @NotNull
  Date getDateAdded() {
    return this.track.getAddedAt();
  }

  @Override
  public @NotNull
  User getWhoAdded() {
    return new SpotifyUser(this.track.getAddedBy());
  }

  @Override
  public boolean isLocal() {
    return this.track.getIsLocal();
  }

  @NotNull
  com.wrapper.spotify.model_objects.specification.PlaylistTrack
  getPlaylistTrack() {
    return this.track;
  }
}
