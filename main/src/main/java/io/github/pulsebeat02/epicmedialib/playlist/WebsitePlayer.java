package io.github.pulsebeat02.epicmedialib.playlist;

import org.jetbrains.annotations.NotNull;

public class WebsitePlayer implements WebPlayer {

  @Override
  public @NotNull String getUrl() {
    return null;
  }

  @Override
  public @NotNull String getCurrentSong() {
    return null;
  }

  @Override
  public @NotNull PlaylistType getType() {
    return null;
  }

  @Override
  public int getIndex() {
    return 0;
  }

  @Override
  public void setIndex(final int index) {}

  @Override
  public void skipSong() {}

  @Override
  public void previousSong() {}

  @Override
  public void pauseSong() {}

  @Override
  public void resumeSong() {}

  @Override
  public void seekToTime(final int seconds) {}

  @Override
  public void randomize() {}

  @Override
  public void loopMode(final boolean mode) {}
}
