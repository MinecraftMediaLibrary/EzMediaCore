package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ImageResource {

  @NotNull
  Image[] getImages();
}
