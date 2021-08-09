package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.playlist.Downloader;
import org.jetbrains.annotations.NotNull;

public interface TrackDownloader extends Downloader {

  @NotNull
  Track getTrack();

  @NotNull
  QuerySearch getSearcher();
}
