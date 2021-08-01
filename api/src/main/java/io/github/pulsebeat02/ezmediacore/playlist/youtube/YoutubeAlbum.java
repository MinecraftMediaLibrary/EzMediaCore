package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import io.github.pulsebeat02.ezmediacore.playlist.Album;
import org.jetbrains.annotations.NotNull;

public interface YoutubeAlbum extends Album {

  @NotNull
  String getAuthor();
}
