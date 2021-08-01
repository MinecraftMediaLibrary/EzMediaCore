package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import io.github.pulsebeat02.epicmedialib.playlist.ResourceUrl;
import org.jetbrains.annotations.NotNull;

public interface User extends ExternalUrl, Followable, ImageResource, ResourceUrl {

  @NotNull
  String getBirthday();

  @NotNull
  String getDisplayName();

  @NotNull
  String getEmail();

  @NotNull
  Subscription getSubscription();
}
