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

package io.github.pulsebeat02.ezmediacore.jlibdl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.MediaInfo;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class YoutubeDLRequest {

  private static final String API_REQUEST_BASE;
  private static final HttpClient HTTP_CLIENT;
  private static final Gson GSON;

  static {
    API_REQUEST_BASE = "https://emc-youtube-dl-backup.herokuapp.com/api/info?url=%s";
    HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    GSON = new Gson();
  }

  @SerializedName("info")
  @Expose
  private MediaInfo info;

  @SerializedName("url")
  @Expose
  private String url;

  public @NotNull YoutubeDLRequest request(@NotNull final String url)
      throws IOException, InterruptedException {
    checkNotNull(url, "URL cannot be null!");
    return GSON.fromJson(this.getJson(url), YoutubeDLRequest.class);
  }

  private @NotNull String getJson(@NotNull final String url)
      throws IOException, InterruptedException {
    return HTTP_CLIENT.send(this.createRequest(url), this.createResponse()).body();
  }

  private @NotNull HttpRequest createRequest(@NotNull final String url) {
    return HttpRequest.newBuilder().uri(URI.create(API_REQUEST_BASE.formatted(url))).build();
  }

  private @NotNull HttpResponse.BodyHandler<String> createResponse() {
    return HttpResponse.BodyHandlers.ofString();
  }

  public @Nullable MediaInfo getInfo() {
    return this.info;
  }

  public @Nullable String getUrl() {
    return this.url;
  }
}
