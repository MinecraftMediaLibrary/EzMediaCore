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
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.jlibdl.JLibDL;
import io.github.pulsebeat02.ezmediacore.jlibdl.YoutubeDLRequest;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class RequestUtils {

  private static final LoadingCache<String, Optional<MediaRequest>> CACHED_RESULT;
  private static final HttpClient HTTP_CLIENT;

  static {
    HTTP_CLIENT =
        HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    CACHED_RESULT =
        Caffeine.newBuilder()
            .executor(ExecutorProvider.SHARED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues()
            .build(RequestUtils::getRequestInternal);
  }

  private static @NotNull Optional<MediaRequest> getRequestInternal(@NotNull final String url)
      throws InterruptedException {
    try {
      final YoutubeDLRequest request = JLibDL.request(url);
      return Optional.of(MediaRequest.ofRequest(request));
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  private RequestUtils() {}

  public static @NotNull Path downloadFile(@NotNull final Path path, @NotNull final String url)
      throws IOException, InterruptedException {
    checkNotNull(path, "Path cannot be null!");
    checkNotNull(url, "URL cannot be null!");
    return HTTP_CLIENT.send(createRequest(url), BodyHandlers.ofFile(path)).body();
  }

  public static @NotNull MediaRequest requestMediaInformation(@NotNull final Input url) {
    final Optional<MediaRequest> result = CACHED_RESULT.get(url.getInput());
    return result.orElseGet(() -> MediaRequest.ofRequest(url));
  }

  private static @NotNull HttpRequest createRequest(@NotNull final String url) {
    checkNotNull(url, "URL cannot be null!");
    return HttpRequest.newBuilder().uri(URI.create(url)).build();
  }
}
