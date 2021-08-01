package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.playlist.Identifier;
import io.github.pulsebeat02.ezmediacore.playlist.Namespace;
import io.github.pulsebeat02.ezmediacore.playlist.ResourceUrl;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface Track extends Namespace, Identifier, ResourceUrl, ExternalUrl {

  @NotNull
  List<Artist> getArtists();

  @NotNull
  String getPreviewUrl();

  int getDiscNumber();

  int getDuration();

  boolean isExplicit();

  boolean isPlayable();
}
