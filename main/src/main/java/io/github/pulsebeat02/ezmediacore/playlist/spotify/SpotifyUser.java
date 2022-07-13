/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.enums.ProductType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class SpotifyUser implements User {

  private static final BiMap<ProductType, Subscription> SUBSCRIPTIONS;

  static {
    SUBSCRIPTIONS =
        HashBiMap.create(
            Map.of(
                ProductType.BASIC_DESKTOP, Subscription.BASIC_DESKTOP,
                ProductType.DAYPASS, Subscription.DAYPASS,
                ProductType.FREE, Subscription.FREE,
                ProductType.OPEN, Subscription.OPEN,
                ProductType.PREMIUM, Subscription.PREMIUM));
  }

  private final se.michaelthelin.spotify.model_objects.specification.User user;
  private final String url;
  private final Avatar[] avatars;

  SpotifyUser(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    checkNotNull(url, "URL cannot be null!");
    this.url = url;
    this.user = this.getInternalUser();
    this.avatars = this.getInternalAvatars();
  }

  SpotifyUser(@NotNull final se.michaelthelin.spotify.model_objects.specification.User user) {
    this.url = user.getUri();
    this.user = user;
    this.avatars = this.getInternalAvatars();
  }

  @Contract("_ -> new")
  public static @NotNull SpotifyUser ofSpotifyUser(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    return new SpotifyUser(url);
  }

  static @NotNull BiMap<ProductType, Subscription> getSubscriptionMappings() {
    return SUBSCRIPTIONS;
  }

  private @NotNull se.michaelthelin.spotify.model_objects.specification.User getInternalUser()
      throws IOException, ParseException, SpotifyWebApiException {
    return SpotifyProvider.getSpotifyApi()
        .getUsersProfile(MediaExtractionUtils.getSpotifyIDExceptionally(this.url))
        .build()
        .execute();
  }

  private Avatar @NotNull [] getInternalAvatars() {
    return Stream.of(this.user.getImages()).map(SpotifyAvatar::new).toArray(SpotifyAvatar[]::new);
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

  @NotNull
  se.michaelthelin.spotify.model_objects.specification.User getUser() {
    return this.user;
  }
}
