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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.jlibdl.Format;
import io.github.pulsebeat02.ezmediacore.jlibdl.JLibDL;
import io.github.pulsebeat02.ezmediacore.jlibdl.MediaInfo;
import io.github.pulsebeat02.ezmediacore.jlibdl.YoutubeDLRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class RequestUtils {

  private static final LoadingCache<String, Optional<YoutubeDLRequest>> CACHED_RESULT;
  private static final JLibDL JLIBDL;

  static {
    CACHED_RESULT =
        Caffeine.newBuilder()
            .executor(ExecutorProvider.SHARED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues()
            .build(RequestUtils::getRequestInternal);
    JLIBDL = new JLibDL();
  }

  private RequestUtils() {}

  public static @NotNull String getSearchedVideos(
      @NotNull final String apiKey, @NotNull final String keyword) {
    final JsonObject obj =
        JsonParser.parseString(
                getResult(
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=%s&key=%s"
                        .formatted(keyword.replace(" ", "+"), apiKey)))
            .getAsJsonObject()
            .getAsJsonArray("items")
            .get(0)
            .getAsJsonObject()
            .getAsJsonObject("snippet");
    return "";
  }

  public static @NotNull String getResult(@NotNull final String link) {
    final StringBuilder result = new StringBuilder();
    try {
      final URL url = new URL(link);
      final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      try (final BufferedReader reader =
          new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        for (String line; (line = reader.readLine()) != null; ) {
          result.append(line);
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return result.toString();
  }

  public static @NotNull String getParentUrl(@NotNull final String link) {
    final int index = link.lastIndexOf('/');
    return index > 0 ? "%s/".formatted(link.substring(0, index)) : "/";
  }

  public static boolean isStream(@NotNull final String url) {

    final YoutubeDLRequest request;
    try {
      request = JLIBDL.request(url);
    } catch (final IOException | InterruptedException e) {
      return false;
    }

    if (request == null) {
      return false;
    }

    final MediaInfo info = request.getInfo();
    if (info == null) {
      return false;
    }

    return info.isLive();
  }

  public static @NotNull List<String> getVideoURLs(@NotNull final String url) {
    final Optional<YoutubeDLRequest> optional = validatePrimaryRequest(url);
    if (optional.isEmpty()) {
      return List.of();
    }
    return List.copyOf(getFormats(optional.get(), true));
  }

  public static @NotNull @Unmodifiable List<String> getAudioURLs(@NotNull final String url) {
    final Optional<YoutubeDLRequest> optional = validatePrimaryRequest(url);
    if (optional.isEmpty()) {
      return List.of();
    }
    return List.copyOf(getFormats(optional.get(), false));
  }

  private static @NotNull Optional<YoutubeDLRequest> validatePrimaryRequest(
      @NotNull final String url) {
    final Optional<YoutubeDLRequest> optional = CACHED_RESULT.get(url);
    if (optional.isEmpty()) {
      return Optional.empty();
    }
    final YoutubeDLRequest request = optional.get();
    if (!validFormats(request)) {
      return Optional.empty();
    }
    return Optional.of(request);
  }

  private static @NotNull List<String> getFormats(
      @NotNull final YoutubeDLRequest request, final boolean video) {
    final List<String> urls = Lists.newArrayList();
    for (final Format format : request.getInfo().getFormats()) {
      if (format == null) {
        continue;
      }
      final String vcodec = format.getVcodec();
      final String acodec = format.getAcodec();
      String url = null;
      if (video) {
        if (vcodec != null && !vcodec.equals("none")) {
          url = format.getUrl();
        }
      } else {
        if (acodec != null && !acodec.equals("none")) {
          url = format.getUrl();
        }
      }
      if (url != null) {
        urls.add(url);
      }
    }
    return urls;
  }

  private static boolean validFormats(@Nullable final YoutubeDLRequest request) {
    if (request == null) {
      return false;
    }
    final MediaInfo info = request.getInfo();
    if (info == null) {
      return false;
    }
    final List<Format> formats = info.getFormats();
    return formats != null && formats.size() >= 1;
  }

  private static @NotNull Optional<YoutubeDLRequest> getRequestInternal(@NotNull final String url)
      throws IOException, InterruptedException {
    return Optional.ofNullable(JLIBDL.request(url));
  }
}
