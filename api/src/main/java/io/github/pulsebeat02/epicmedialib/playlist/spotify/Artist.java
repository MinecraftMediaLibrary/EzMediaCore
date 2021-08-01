package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import io.github.pulsebeat02.epicmedialib.playlist.Identifier;
import io.github.pulsebeat02.epicmedialib.playlist.Namespace;
import io.github.pulsebeat02.epicmedialib.playlist.ResourceUrl;
import org.jetbrains.annotations.NotNull;

public interface Artist
    extends Namespace, Identifier, ResourceUrl, ExternalUrl, Followable, ImageResource {

  int getPopularity();

  @NotNull
  String[] getGenres();
}
