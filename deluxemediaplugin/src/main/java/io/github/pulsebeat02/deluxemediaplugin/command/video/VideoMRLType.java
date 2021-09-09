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
package io.github.pulsebeat02.deluxemediaplugin.command.video;

import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public enum VideoMRLType {
  SPOTIFY,
  YOUTUBE,
  LOCAL_FILE,
  DIRECT_URL;

  public static Optional<VideoMRLType> getType(@NotNull final String mrl) {
    return isMrlYoutube(mrl)
        ? Optional.of(YOUTUBE)
        : isMrlSpotify(mrl)
            ? Optional.of(SPOTIFY)
            : isMrlLocalPath(mrl)
                ? Optional.of(LOCAL_FILE)
                : isMrlDirectUrl(mrl) ? Optional.of(DIRECT_URL) : Optional.empty();
  }

  public static boolean isMrlYoutube(@NotNull final String mrl) {
    return MediaExtractionUtils.getYoutubeID(mrl).isPresent();
  }

  public static boolean isMrlLocalPath(@NotNull final String mrl) {
    try {
      return Files.exists(Path.of(mrl));
    } catch (final InvalidPathException e) {
      return false;
    }
  }

  public static boolean isMrlSpotify(@NotNull final String mrl) {
    return MediaExtractionUtils.getSpotifyID(mrl).isPresent();
  }

  public static boolean isMrlDirectUrl(@NotNull final String mrl) {
    try {
      new URL(mrl);
    } catch (final MalformedURLException e) {
      return false;
    }
    return StringUtils.endsWithIgnoreCase(mrl, "mp4");
  }
}
