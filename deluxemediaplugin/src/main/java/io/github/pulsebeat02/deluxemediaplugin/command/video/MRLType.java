package io.github.pulsebeat02.deluxemediaplugin.command.video;

import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum MRLType {
  SPOTIFY,
  YOUTUBE,
  LOCAL_FILE,
  DIRECT_URL;

  public static Optional<MRLType> getType(@NotNull final String mrl) {
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
    return mrl.endsWith(".mp4");
  }
}
