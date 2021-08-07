package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResponseUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class YoutubeVideo implements Video {

  private final String url;
  private final String id;
  private final VideoInfo video;
  private final VideoDetails details;
  private final List<VideoFormat> videoFormats;
  private final List<AudioFormat> audioFormats;

  public YoutubeVideo(@NotNull final String url) {
    this.url = url;
    this.id = MediaExtractionUtils.getYoutubeID(url)
        .orElseThrow(() -> new DeadResourceLinkException(url));
    this.video = this.getVideoResponse();
    this.details = this.video.details();
    this.videoFormats =
        this.video.videoFormats().stream()
            .map(YoutubeVideoFormat::new)
            .collect(Collectors.toList());
    this.audioFormats =
        this.video.audioFormats().stream()
            .map(YoutubeAudioFormat::new)
            .collect(Collectors.toList());
  }

  private VideoInfo getVideoResponse() {
    return ResponseUtils.getResponseResult(YoutubeProvider.getYoutubeDownloader()
        .getVideoInfo(new RequestVideoInfo(this.id))).orElseGet(this::getVideoResponse);
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
  public @NotNull List<String> getKeywords() {
    return this.details.keywords();
  }

  @Override
  public @NotNull List<VideoFormat> getVideoFormats() {
    return this.videoFormats;
  }

  @Override
  public @NotNull List<AudioFormat> getAudioFormats() {
    return this.audioFormats;
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

  @NotNull VideoInfo getVideoInfo() {
    return this.video;
  }
}
