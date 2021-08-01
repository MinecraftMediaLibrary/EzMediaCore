package io.github.pulsebeat02.epicmedialib.playlist;

import org.jetbrains.annotations.NotNull;

public interface WebPlayer extends WebPlayerControls, ResourceUrl {

  @NotNull
  String getCurrentSong();

  @NotNull
  PlaylistType getType();

  int getIndex();

  void setIndex(final int index);
}
