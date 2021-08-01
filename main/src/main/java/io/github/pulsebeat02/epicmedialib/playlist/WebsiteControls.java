package io.github.pulsebeat02.epicmedialib.playlist;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class WebsiteControls implements WebPlayerControls {

  private final String url;
  private final Collection<String> songs;
  private final PlaylistType type;
  private int index;

  public WebsiteControls(final String url, @NotNull final PlaylistType type) {
    this.url = url;
    this.songs = getSongs();
    this.type = type;
  }

  private List<String> getSongs() {
    // TODO: 7/30/2021 get songs from spotify using some api
    return Collections.emptyList();
  }

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
