package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResponseUtils;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class YoutubeVideoDownloader implements VideoDownloader {

  private final YoutubeVideo video;
  private final Path videoPath;

  public YoutubeVideoDownloader(@NotNull final String url, @NotNull final Path videoPath) {
    this(new YoutubeVideo(url), videoPath);
  }

  public YoutubeVideoDownloader(@NotNull final YoutubeVideo video, @NotNull final Path videoPath) {
    this.video = video;
    this.videoPath = videoPath;
  }

  public YoutubeVideoDownloader(
      @NotNull final MediaLibraryCore core,
      @NotNull final YoutubeVideo video,
      @NotNull final String fileName) {
    this.video = video;
    this.videoPath = core.getVideoPath().resolve(fileName);
  }

  public YoutubeVideoDownloader(
      @NotNull final MediaLibraryCore core,
      @NotNull final String url,
      @NotNull final String fileName) {
    this(core, new YoutubeVideo(url), fileName);
  }

  @Override
  public void downloadVideo(@NotNull final VideoQuality format, final boolean overwrite) {
    this.onStartVideoDownload();

    this.internalDownload(new RequestVideoFileDownload(this.getFormat(format))
        .saveTo(this.videoPath.getParent().toFile())
        .renameTo(FilenameUtils.removeExtension(PathUtils.getName(this.videoPath)))
        .overwriteIfExists(overwrite));

    this.onFinishVideoDownload();
  }

  private void internalDownload(@NotNull final RequestVideoFileDownload download) {
    if (ResponseUtils.getResponseResult(
        YoutubeProvider.getYoutubeDownloader().downloadVideoFile(download)).isEmpty()) {
      this.internalDownload(download);
    }
  }

  private com.github.kiulian.downloader.model.videos.formats.VideoFormat getFormat(
      @NotNull final VideoQuality format) {
    return this.video.getVideoInfo().videoFormats().stream()
        .filter(
            f ->
                f.videoQuality()
                    .equals(YoutubeVideoFormat.getVideoFormatMappings().inverse().get(format)))
        .findAny()
        .orElseThrow(IllegalArgumentException::new);
  }

  @Override
  public void onStartVideoDownload() {
  }

  @Override
  public void onFinishVideoDownload() {
  }

  @Override
  public @NotNull Video getVideo() {
    return this.video;
  }

  @Override
  public @NotNull Path getDownloadPath() {
    return this.videoPath;
  }
}
