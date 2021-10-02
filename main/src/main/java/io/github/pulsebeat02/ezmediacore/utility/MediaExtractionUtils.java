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
package io.github.pulsebeat02.ezmediacore.utility;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
  }

  private MediaExtractionUtils() {
  }

  /**
   * Extracts id from Youtube URL.
   *
   * @param url the url
   * @return an Optional containing the String url if existing
   */
  @NotNull
  public static Optional<String> getYoutubeID(@NotNull final String url) {
    final Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
    if (matcher.find()) {
      return Optional.of(matcher.group());
    }
    return Optional.empty();
  }

  /**
   * Extracts id from Spotify URL.
   *
   * @param url the url
   * @return an Optional containing the String url if existing
   */
  @NotNull
  public static Optional<String> getSpotifyID(@NotNull final String url) {
    if (!url.contains("spotify")) {
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

  @NotNull
  public static Optional<String> getFirstResultVideo(@NotNull final String query) {
    return CACHED_RESULT.get(query.trim().toLowerCase(Locale.ROOT));
  }

  @NotNull
  private static Optional<String> getFirstResultVideoInternal(@NotNull final String query)
      throws IOException {
    try (final InputStream in =
        new URL(YOUTUBE_SEARCH_URL.formatted(query.replaceAll(" ", "+"))).openStream()) {
      final String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      final int start = FastStringUtils.fastQuerySearch(content, SEARCH_KEYWORD) + 10;
      return Optional.of(content.substring(start, content.indexOf('"', start)));
    }
  }

  @NotNull
  public static FrameConfiguration getFrameConfiguration(
      @NotNull final MediaLibraryCore core, @NotNull final Path path) throws IOException {
    return FrameConfiguration.ofFps(VideoFrameUtils.getFrameRate(core, path).orElseThrow());
  }
}
