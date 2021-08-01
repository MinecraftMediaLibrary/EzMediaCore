package io.github.pulsebeat02.epicmedialib.utility;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class MediaExtractionUtils {

  private static final Pattern YOUTUBE_ID_PATTERN;

  static {
    YOUTUBE_ID_PATTERN = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed)[^#]*");
  }

  private MediaExtractionUtils() {}

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
}
