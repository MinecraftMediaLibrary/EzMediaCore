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
package io.github.pulsebeat02.ezmediacore.utility.media;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.throwable.UnknownArtistException;
import io.github.pulsebeat02.ezmediacore.utility.manipulation.FastStringUtils;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class MediaExtractionUtils {

  private static final LoadingCache<String, Optional<String>> CACHED_RESULT;
  private static final Pattern YOUTUBE_ID_PATTERN;
  private static final String YOUTUBE_SEARCH_URL;
  private static final String SEARCH_KEYWORD;
  private static final HttpClient HTTP_CLIENT;

  static {
    CACHED_RESULT =
        Caffeine.newBuilder()
            .executor(ExecutorProvider.SHARED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues()
            .build(MediaExtractionUtils::getFirstResultVideoInternal);
    YOUTUBE_ID_PATTERN = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed)[^#]*");
    YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query=%s";
    SEARCH_KEYWORD = "videoId";
    HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  private MediaExtractionUtils() {}

  /**
   * Extracts id from Youtube URL.
   *
   * @param url the url
   * @return an Optional containing the String url if existing
   */
  public static @NotNull Optional<String> getYoutubeID(@NotNull final String url) {
    checkNotNull(url, "Youtube Video URL cannot be null!");
    final Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
    if (matcher.find()) {
      return Optional.of(matcher.group());
    }
    return Optional.empty();
  }

  public static @NotNull String getYoutubeIDExceptionally(@NotNull final String url) {
    checkNotNull(url, "Youtube URL cannot be null!");
    return getYoutubeID(url).orElseThrow(() -> new DeadResourceLinkException(url));
  }

  /**
   * Extracts id from Spotify URL.
   *
   * @param url the url
   * @return an Optional containing the String url if existing
   */
  public static @NotNull Optional<String> getSpotifyID(@NotNull final String url) {
    checkNotNull(url, "Spotify URL cannot be null!");
    if (!isSpotifyLink(url)) {
      return Optional.empty();
    }
    final String[] split = url.split("\\?")[0].split("/");
    final String id = split[split.length - 1];
    if (id.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(id);
    }
  }

  private static boolean isSpotifyLink(@NotNull final String url) {
    return url.contains("spotify");
  }

  public static @NotNull String getSpotifyIDExceptionally(@NotNull final String url) {
    checkNotNull(url, "Spotify URL cannot be null!");
    return getSpotifyID(url).orElseThrow(() -> new UnknownArtistException(url));
  }

  public static @NotNull Optional<String> getFirstResultVideo(@NotNull final String query) {
    checkQuery(query);
    return CACHED_RESULT.get(query.trim().toLowerCase(Locale.ROOT));
  }

  public static @NotNull String getFirstResultVideoExceptionally(@NotNull final String query) {
    checkQuery(query);
    return getFirstResultVideo(query).orElseThrow(() -> new DeadResourceLinkException(query));
  }

  private static void checkQuery(@NotNull final String query) {
    checkNotNull(query, "Query cannot be null!");
    checkArgument(query.length() != 0, "Query cannot be empty!");
  }

  private static @NotNull Optional<String> getFirstResultVideoInternal(@NotNull final String query)
      throws IOException, InterruptedException {
    final String content = HTTP_CLIENT.send(createRequest(query), createBodyHandler()).body();
    final int start = FastStringUtils.fastQuerySearch(content, SEARCH_KEYWORD) + 10;
    return Optional.of(content.substring(start, content.indexOf('"', start)));
  }

  private static @NotNull BodyHandler<String> createBodyHandler() {
    return HttpResponse.BodyHandlers.ofString();
  }

  private static @NotNull HttpRequest createRequest(@NotNull final String query) {
    return HttpRequest.newBuilder()
        .uri(URI.create(YOUTUBE_SEARCH_URL.formatted(query.replaceAll(" ", "+"))))
        .build();
  }
}
