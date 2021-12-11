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

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.jlibdl.JLibDL;
import io.github.pulsebeat02.ezmediacore.jlibdl.YoutubeDLRequest;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.Format;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.MediaInfo;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RequestUtils {

  private static final LoadingCache<String, Optional<YoutubeDLRequest>> CACHED_RESULT;
  private static final HttpClient HTTP_CLIENT;

  static {
    CACHED_RESULT =
        Caffeine.newBuilder()
            .executor(ExecutorProvider.SHARED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues()
            .build(RequestUtils::getRequestInternal);
    HTTP_CLIENT =
        HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
  }

  private RequestUtils() {}

  public static @NotNull String getResult(@NotNull final String link) {
    checkNotNull(link, "URL cannot be null!");
    try {
      final HttpResponse<String> response =
          HTTP_CLIENT.send(createRequest(link), createBodyHandler());
      return response.body();
    } catch (final IOException | InterruptedException e) {
      e.printStackTrace();
    }
    throw new AssertionError("Empty content from url %s!".formatted(link));
  }

  @Contract(pure = true)
  private static @NotNull BodyHandler<String> createBodyHandler() {
    return HttpResponse.BodyHandlers.ofString();
  }

  public static boolean isStream(@NotNull final MrlConfiguration url) {
    try {
      return JLibDL.request(url.getMrl()).getInfo().isLive();
    } catch (final IOException | InterruptedException e) {
      return false;
    }
  }

  public static @NotNull List<MrlConfiguration> getVideoURLs(@NotNull final MrlConfiguration url) {
    final Optional<YoutubeDLRequest> optional = validatePrimaryRequest(url);
    if (optional.isEmpty()) {
      return List.of(url);
    }
    return List.copyOf(getFormats(optional.get(), url, true));
  }

  public static @NotNull List<MrlConfiguration> getAudioURLs(@NotNull final MrlConfiguration url) {
    final Optional<YoutubeDLRequest> optional = validatePrimaryRequest(url);
    if (optional.isEmpty()) {
      return List.of(url);
    }
    return List.copyOf(getFormats(optional.get(), url, false));
  }

  public static @NotNull Path downloadFile(@NotNull final Path path, @NotNull final String url)
      throws IOException, InterruptedException {
    checkNotNull(path, "Path cannot be null!");
    checkNotNull(url, "URL cannot be null!");
    return HTTP_CLIENT.send(createRequest(url), BodyHandlers.ofFile(path)).body();
  }

  private static @NotNull HttpRequest createRequest(@NotNull final String url) {
    checkNotNull(url, "URL cannot be null!");
    return HttpRequest.newBuilder().uri(URI.create(url)).build();
  }

  private static @NotNull Optional<YoutubeDLRequest> validatePrimaryRequest(
      @NotNull final MrlConfiguration url) {
    final Optional<YoutubeDLRequest> optional = CACHED_RESULT.get(url.getMrl());
    if (optional.isEmpty()) {
      return Optional.empty();
    }
    final YoutubeDLRequest request = optional.get();
    if (!validFormats(request)) {
      return Optional.empty();
    }
    return Optional.of(request);
  }

  private static @NotNull List<MrlConfiguration> getFormats(
      @NotNull final YoutubeDLRequest request,
      @NotNull final MrlConfiguration mrl,
      final boolean video) {
    final List<MrlConfiguration> urls = Lists.newArrayList();
    for (final Format format : request.getInfo().getFormats()) {
      urls.add(getLinkMrl(format, video));
    }
    if (urls.size() == 0) {
      urls.add(mrl);
    }
    return urls;
  }

  private static @NotNull MrlConfiguration getLinkMrl(
      @NotNull final Format format, final boolean video) {
    return MrlConfiguration.ofMrl(
        getProperUrl(format.getAcodec(), format.getVcodec(), format, video));
  }

  private @NotNull static String getProperUrl(
      @NotNull final String acodec,
      @NotNull final String vcodec,
      final Format format,
      final boolean video) {
    String url = null;
    if (video) {
      if (!vcodec.equals("none")) {
        url = format.getUrl();
      }
    } else {
      if (!acodec.equals("none")) {
        url = format.getUrl();
      }
    }
    return url;
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
    return Optional.ofNullable(JLibDL.request(url));
  }
}
