package io.github.pulsebeat02.ezmediacore.jlibdl;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jetbrains.annotations.Nullable;

public final class YoutubeDLRequest {

  private static final String API_REQUEST_BASE;
  private static final HttpClient HTTP_CLIENT;
  private static final Gson GSON;

  static {
    API_REQUEST_BASE = "https://emc-youtube-dl.herokuapp.com/api/info?url=%s";
    HTTP_CLIENT = HttpClient.newHttpClient();
    GSON = new Gson();
  }

  @SerializedName("info")
  @Expose
  private MediaInfo info;

  @SerializedName("url")
  @Expose
  private String url;

  static @Nullable YoutubeDLRequest request(@Nullable final String url)
      throws IOException, InterruptedException {
    return GSON.fromJson(
        HTTP_CLIENT
            .send(
                HttpRequest.newBuilder().uri(URI.create(API_REQUEST_BASE.formatted(url))).build(),
                HttpResponse.BodyHandlers.ofString())
            .body(),
        YoutubeDLRequest.class);
  }

  public @Nullable MediaInfo getInfo() {
    return this.info;
  }

  public @Nullable String getUrl() {
    return this.url;
  }
}
