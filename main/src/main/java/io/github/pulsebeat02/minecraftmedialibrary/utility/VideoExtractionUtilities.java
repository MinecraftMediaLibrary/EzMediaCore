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

import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Special video extraction utilities used throughout the library and also open to users. Used for
 * easier video extraction management.
 */
public final class VideoExtractionUtilities {

  private static final Pattern YOUTUBE_ID_PATTERN;

  static {
    YOUTUBE_ID_PATTERN = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed)[^#]*");
  }

  private VideoExtractionUtilities() {}

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
      final String id = matcher.group();
      Logger.info(String.format("Found Video ID for %s(%s)", url, id));
      return Optional.of(id);
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

  /**
   * Create sha1 hash from a file.
   *
   * @param file the file
   * @return the byte [ ]
   */
  public static byte @NotNull [] createHashSHA(@NotNull final File file) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-1");
      final InputStream fis = new FileInputStream(file);
      int n = 0;
      final byte[] buffer = new byte[8192];
      while (n != -1) {
        n = fis.read(buffer);
        if (n > 0) {
          digest.update(buffer, 0, n);
        }
      }
      final byte[] hash = digest.digest();
      Logger.info(
          String.format(
              "Generated Hash for File %s (%s)", file.getAbsolutePath(), new String(hash)));
      return hash;
    } catch (final IOException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return new byte[] {};
  }
}
