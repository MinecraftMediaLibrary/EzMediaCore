package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.playlist.Identifier;
import io.github.pulsebeat02.ezmediacore.playlist.Namespace;
import io.github.pulsebeat02.ezmediacore.playlist.ResourceUrl;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface Track extends Namespace, Identifier, ResourceUrl, ExternalUrl {

  @NotNull
  Collection<Artist> getArtists();

  @NotNull
  String getPreviewUrl();

  int getDiscNumber();

  int getDuration();

  boolean isExplicit();

  boolean isPlayable();
}
