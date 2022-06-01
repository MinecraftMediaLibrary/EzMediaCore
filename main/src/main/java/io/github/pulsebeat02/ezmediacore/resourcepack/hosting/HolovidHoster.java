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
package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class HolovidHoster implements HolovidSolution {

  private static final LoadingCache<String, Optional<HolovidResourcepackResult>> CACHED_RESULT;
  private static final String HOLOVID_LINK;
  private static final HttpClient HTTP_CLIENT;

  static {
    CACHED_RESULT =
        Caffeine.newBuilder()
            .executor(ExecutorProvider.SHARED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues()
            .build(HolovidHoster::getResourcpackUrlInternal);
    HOLOVID_LINK = "https://holovid.glare.dev/resourcepack/download?videoUrl=";
    HTTP_CLIENT = HttpClient.newHttpClient();
  }

  public HolovidHoster() {}

  private static Optional<HolovidResourcepackResult> getResourcpackUrlInternal(
      @NotNull final String url) {
    try {
      final HolovidResourcepackResult result =
          GsonProvider.getSimple()
              .fromJson(getRequestInternal(url), HolovidResourcepackResult.class);
      return Optional.of(result);
    } catch (final IOException | URISyntaxException | InterruptedException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  private static @NotNull String getRequestInternal(@NotNull final String input)
      throws URISyntaxException, IOException, InterruptedException {
    return HTTP_CLIENT.send(createRequestInternal(input), createBodyHandlerInternal()).body();
  }

  private static @NotNull HttpRequest createRequestInternal(@NotNull final String input)
      throws URISyntaxException {
    return HttpRequest.newBuilder().uri(new URI(HOLOVID_LINK + input)).build();
  }

  private static @NotNull BodyHandler<String> createBodyHandlerInternal() {
    return HttpResponse.BodyHandlers.ofString();
  }

  @Override
  public @NotNull String createUrl(@NotNull final String input) {
    checkNotNull(input, "Query cannot be null!");
    checkArgument(input.length() != 0, "Query cannot be empty!");
    return CACHED_RESULT
        .get(input.trim().toLowerCase(Locale.ROOT))
        .orElseThrow(AssertionError::new)
        .getUrl();
  }
}
