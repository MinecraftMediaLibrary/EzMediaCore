package io.github.pulsebeat02.epicmedialib.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public final class RequestUtils {

  private RequestUtils() {}

  public static String getSearchedVideos(
      @NotNull final String apiKey, @NotNull final String keyword) {
    final JsonObject obj =
        JsonParser.parseString(
                getResult(
                    String.format(
                        "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=%s&key=%s",
                        keyword.replace(" ", "+"), apiKey)))
            .getAsJsonObject()
            .getAsJsonArray("items")
            .get(0)
            .getAsJsonObject()
            .getAsJsonObject("snippet");
    return "";
  }

  public static String getResult(@NotNull final String link) {
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
}
