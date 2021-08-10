package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.github.kiulian.downloader.YoutubeDownloader;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.NotNull;

public final class YoutubeProvider {

  private static final YoutubeDownloader YOUTUBE_DOWNLOADER;

  static {
    YOUTUBE_DOWNLOADER = new YoutubeDownloader();
  }

  private YoutubeProvider() {
  }

  public static void initialize(@NotNull final MediaLibraryCore core) {
  }

  static YoutubeDownloader getYoutubeDownloader() {
    return YOUTUBE_DOWNLOADER;
  }
}
