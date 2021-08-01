package io.github.pulsebeat02.epicmedialib.playlist.youtube;

import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import io.github.pulsebeat02.epicmedialib.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.epicmedialib.utility.MediaExtractionUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class YoutubeVideo implements Video {

  private final String url;
  private final VideoInfo video;
  private final VideoDetails details;

  public YoutubeVideo(@NotNull final String url) {
    this.url = url;
    this.video =
        YoutubeProvider.getYoutubeDownloader()
            .getVideoInfo(
                new RequestVideoInfo(
                    MediaExtractionUtils.getYoutubeID(url)
                        .orElseThrow(() -> new DeadResourceLinkException(url))))
            .data();
    this.details = this.video.details();
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull String getId() {
    return this.details.videoId();
  }

  @Override
  public @NotNull String getLiveUrl() {
    return this.details.liveUrl();
  }

  @Override
  public @NotNull Collection<String> getKeywords() {
    return this.details.keywords();
  }

  @Override
  public @NotNull Collection<VideoFormat> getVideoFormats() {
    return this.video.videoFormats().stream()
        .map(YoutubeVideoFormat::new)
        .collect(Collectors.toList());
  }

  @Override
  public @NotNull Collection<AudioFormat> getAudioFormats() {
    return this.video.audioFormats().stream()
        .map(YoutubeAudioFormat::new)
        .collect(Collectors.toList());
  }

  @Override
  public long getViewCount() {
    return this.details.viewCount();
  }

  @Override
  public int getAverageRating() {
    return this.details.averageRating();
  }

  @Override
  public boolean isLiveContent() {
    return this.details.isLiveContent();
  }

  protected @NotNull VideoInfo getVideoInfo() {
    return this.video;
  }
}
