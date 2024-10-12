/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.jlibdl.JLibDL;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.MediaInfo;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class RequestUtils {

  private static final LoadingCache<String, Optional<MediaRequest>> CACHED_RESULT;
  private static final int DOWNLOAD_CHUNK_SIZE;

  static {
    CACHED_RESULT =
        Caffeine.newBuilder()
            .executor(ExecutorProvider.SHARED_RESULT_POOL)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .softValues()
            .build(RequestUtils::getRequestInternal);
    DOWNLOAD_CHUNK_SIZE = 5 * 1_000 * 1_000;
  }

  private RequestUtils() {}

  private static Optional<MediaRequest> getRequestInternal( final String url)
      throws InterruptedException {
    try {
      final MediaInfo request = JLibDL.request(url);
      if (request == null) {
        return Optional.empty();
      }
      return Optional.of(MediaRequest.ofRequest(request));
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  public static MediaRequest requestMediaInformation( final Input url) {
    final String input = url.getInput();
    final Optional<MediaRequest> result = CACHED_RESULT.get(input);
    return result.orElseGet(MediaRequest::ofEmptyRequest);
  }
}
