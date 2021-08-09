package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.playlist.youtube.Video;
import org.jetbrains.annotations.NotNull;

public interface QuerySearch {

  @NotNull Video getYoutubeVideo();

  @NotNull
  String getSearchQuery();

  @NotNull
  Track getTrack();

}
