package io.github.pulsebeat02.epicmedialib.playlist.youtube;

import io.github.pulsebeat02.epicmedialib.playlist.Album;
import org.jetbrains.annotations.NotNull;

public interface YoutubeAlbum extends Album {

  @NotNull
  String getAuthor();
}
