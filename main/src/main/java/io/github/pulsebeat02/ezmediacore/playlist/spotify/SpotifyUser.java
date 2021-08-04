package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.wrapper.spotify.enums.ProductType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.ezmediacore.throwable.UnknownPlaylistException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyUser implements User {

  private static final BiMap<ProductType, Subscription> SUBSCRIPTIONS;

  static {
    SUBSCRIPTIONS =
        HashBiMap.create(
            ImmutableMap.of(
                ProductType.BASIC_DESKTOP, Subscription.BASIC_DESKTOP,
                ProductType.DAYPASS, Subscription.DAYPASS,
                ProductType.FREE, Subscription.FREE,
                ProductType.OPEN, Subscription.OPEN,
                ProductType.PREMIUM, Subscription.PREMIUM));
  }

  private final com.wrapper.spotify.model_objects.specification.User user;
  private final String url;
  private final Avatar[] avatars;

  public SpotifyUser(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    this.url = url;
    this.user =
        SpotifyProvider.getSpotifyApi()
            .getUsersProfile(
                MediaExtractionUtils.getSpotifyID(url)
                    .orElseThrow(() -> new UnknownPlaylistException(url)))
            .build()
            .execute();
    this.avatars =
        Arrays.stream(this.user.getImages()).map(SpotifyAvatar::new).toArray(SpotifyAvatar[]::new);
  }

  SpotifyUser(@NotNull final com.wrapper.spotify.model_objects.specification.User user) {
    this.url = user.getUri();
    this.user = user;
    this.avatars =
        Arrays.stream(this.user.getImages()).map(SpotifyAvatar::new).toArray(SpotifyAvatar[]::new);
  }

  protected static @NotNull BiMap<ProductType, Subscription> getSubscriptionMappings() {
    return SUBSCRIPTIONS;
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull Map<String, String> getExternalUrls() {
    return this.user.getExternalUrls().getExternalUrls();
  }

  @Override
  public int getTotalFollowers() {
    return this.user.getFollowers().getTotal();
  }

  @Override
  public @NotNull Avatar[] getImages() {
    return this.avatars;
  }

  @Override
  public @NotNull String getBirthday() {
    return this.user.getBirthdate();
  }

  @Override
  public @NotNull String getDisplayName() {
    return this.user.getDisplayName();
  }

  @Override
  public @NotNull String getEmail() {
    return this.user.getEmail();
  }

  @Override
  public @NotNull Subscription getSubscription() {
    return SUBSCRIPTIONS.get(this.user.getProduct());
  }

  protected @NotNull com.wrapper.spotify.model_objects.specification.User getUser() {
    return this.user;
  }
}
