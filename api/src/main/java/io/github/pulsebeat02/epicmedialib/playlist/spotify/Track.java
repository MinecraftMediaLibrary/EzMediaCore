package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import io.github.pulsebeat02.epicmedialib.playlist.Identifier;
import io.github.pulsebeat02.epicmedialib.playlist.Namespace;
import io.github.pulsebeat02.epicmedialib.playlist.ResourceUrl;
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
