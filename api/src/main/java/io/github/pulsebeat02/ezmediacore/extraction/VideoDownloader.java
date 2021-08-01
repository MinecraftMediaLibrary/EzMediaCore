package io.github.pulsebeat02.ezmediacore.extraction;

import io.github.pulsebeat02.ezmediacore.playlist.youtube.Video;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface VideoDownloader {

  void downloadVideo(@NotNull final VideoQuality format, final boolean overwrite);

  void onStartVideoDownload();

  void onFinishVideoDownload();

  @NotNull
  Video getVideo();

  @NotNull
  Path getDownloadPath();
}
