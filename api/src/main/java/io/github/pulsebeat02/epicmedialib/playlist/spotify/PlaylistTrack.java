package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import java.util.Date;
import org.jetbrains.annotations.NotNull;

public interface PlaylistTrack {

  @NotNull
  Date getDateAdded();

  @NotNull
  User getWhoAdded();

  boolean isLocal();
}
