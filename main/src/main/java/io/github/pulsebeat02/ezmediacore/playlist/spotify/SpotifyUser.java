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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
            Map.of(
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

  static @NotNull BiMap<ProductType, Subscription> getSubscriptionMappings() {
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

  @NotNull
  com.wrapper.spotify.model_objects.specification.User getUser() {
    return this.user;
  }
}
