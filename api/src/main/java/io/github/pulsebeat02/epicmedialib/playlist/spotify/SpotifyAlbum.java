package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import io.github.pulsebeat02.epicmedialib.playlist.Album;
import org.jetbrains.annotations.NotNull;

public interface SpotifyAlbum extends Album, ExternalUrl {

  @NotNull
  User getAuthor();
}
