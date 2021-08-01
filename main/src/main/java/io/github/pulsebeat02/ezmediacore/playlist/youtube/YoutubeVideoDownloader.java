package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import io.github.pulsebeat02.ezmediacore.extraction.VideoDownloader;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import java.io.File;
import java.nio.file.Path;
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

  @Override
  public void downloadVideo(@NotNull final VideoQuality format, final boolean overwrite) {
    onStartVideoDownload();

    final String name = PathUtils.getName(this.videoPath);
    final String outputDirectory = this.videoPath.getParent().toString();

    final RequestVideoFileDownload download =
        new RequestVideoFileDownload(getFormat(format))
            .saveTo(new File(outputDirectory))
            .renameTo(name)
            .overwriteIfExists(overwrite);

    YoutubeProvider.getYoutubeDownloader().downloadVideoFile(download);

    onFinishVideoDownload();
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
  public void onStartVideoDownload() {}

  @Override
  public void onFinishVideoDownload() {}

  @Override
  public @NotNull Video getVideo() {
    return this.video;
  }

  @Override
  public @NotNull Path getDownloadPath() {
    return videoPath;
  }
}
