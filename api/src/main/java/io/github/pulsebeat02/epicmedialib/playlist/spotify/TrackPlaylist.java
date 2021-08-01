package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface TrackPlaylist extends SpotifyAlbum, Followable {

  @NotNull
  Collection<PlaylistTrack> getTracks();

  @NotNull
  String getDescription();

  boolean isCollaborative();

  boolean isPubliclyAccessible();
}
