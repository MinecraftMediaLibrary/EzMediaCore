package io.github.pulsebeat02.ezmediacore.playlist;

import io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoQuality;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface Downloader {

  void downloadVideo(@NotNull final VideoQuality format, final boolean overwrite);

  void onStartVideoDownload();

  void onFinishVideoDownload();

  @NotNull
  Path getDownloadPath();

}
