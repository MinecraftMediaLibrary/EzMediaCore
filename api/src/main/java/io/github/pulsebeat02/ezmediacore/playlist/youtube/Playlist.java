package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface Playlist extends YoutubeAlbum {

  @NotNull
  List<Video> getVideos();

  long getViewCount();
}
