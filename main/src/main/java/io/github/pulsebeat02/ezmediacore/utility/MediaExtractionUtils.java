package io.github.pulsebeat02.ezmediacore.utility;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        Caffeine.newBuilder().executor(ExecutorProvider.CACHED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues().build(MediaExtractionUtils::getFirstResultVideoInternal);
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
    try (final InputStream in = new URL(YOUTUBE_SEARCH_URL.formatted(
        query.replaceAll(" ", "+"))).openStream()) {
      final String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      final int start = FastStringUtils.fastQuerySearch(content, SEARCH_KEYWORD) + 10;
      return Optional.of(content.substring(start, content.indexOf('"', start)));
    }
  }
}
