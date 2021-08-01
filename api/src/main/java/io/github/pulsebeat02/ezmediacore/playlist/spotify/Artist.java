package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.playlist.Identifier;
import io.github.pulsebeat02.ezmediacore.playlist.Namespace;
import io.github.pulsebeat02.ezmediacore.playlist.ResourceUrl;
import org.jetbrains.annotations.NotNull;

public interface Artist
    extends Namespace, Identifier, ResourceUrl, ExternalUrl, Followable, ImageResource {

  int getPopularity();

  @NotNull
  String[] getGenres();
}
