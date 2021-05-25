/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A utility class used to find out information of a video from a request and providing the user
 * with JSON information.
 */
public final class RequestUtilities {

  private RequestUtilities() {}

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

  /**
   * Gets the json result from a request to a specific link.
   *
   * @param link the link
   * @return the resulting output
   */
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
