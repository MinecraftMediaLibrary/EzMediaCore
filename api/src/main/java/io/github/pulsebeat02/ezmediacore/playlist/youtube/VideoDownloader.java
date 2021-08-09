package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import io.github.pulsebeat02.ezmediacore.playlist.Downloader;
import org.jetbrains.annotations.NotNull;

public interface VideoDownloader extends Downloader {

  @NotNull
  Video getVideo();

}
