package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface TrackPlaylist extends SpotifyAlbum, Followable {

  @NotNull
  List<PlaylistTrack> getTracks();

  @NotNull
  String getDescription();

  boolean isCollaborative();

  boolean isPubliclyAccessible();
}
