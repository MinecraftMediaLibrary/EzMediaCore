package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import org.jetbrains.annotations.NotNull;

public final class SpotifyAvatar implements Avatar {

  private final com.wrapper.spotify.model_objects.specification.Image image;
  private final Dimension dimension;

  SpotifyAvatar(@NotNull final com.wrapper.spotify.model_objects.specification.Image image) {
    this.image = image;
    this.dimension = Dimension.of(image.getWidth(), image.getHeight());
  }

  @Override
  public @NotNull String getUrl() {
    return this.image.getUrl();
  }

  @NotNull
  com.wrapper.spotify.model_objects.specification.Image getImage() {
    return this.image;
  }

  @Override
  public @NotNull Dimension getDimensions() {
    return this.dimension;
  }
}
