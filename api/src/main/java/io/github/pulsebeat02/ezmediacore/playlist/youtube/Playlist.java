package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface Playlist extends YoutubeAlbum {

  @NotNull
  Collection<Video> getVideos();

  long getViewCount();
}
