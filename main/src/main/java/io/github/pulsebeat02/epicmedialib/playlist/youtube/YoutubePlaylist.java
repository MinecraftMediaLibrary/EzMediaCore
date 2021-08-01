package io.github.pulsebeat02.epicmedialib.playlist.youtube;

import com.github.kiulian.downloader.downloader.request.RequestPlaylistInfo;
import com.github.kiulian.downloader.model.playlist.PlaylistDetails;
import com.github.kiulian.downloader.model.playlist.PlaylistInfo;
import io.github.pulsebeat02.epicmedialib.throwable.UnknownPlaylistException;
import io.github.pulsebeat02.epicmedialib.utility.MediaExtractionUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class YoutubePlaylist implements Playlist {

  private final String url;
  private final PlaylistInfo info;
  private final PlaylistDetails details;

  public YoutubePlaylist(@NotNull final String url) {
    this.url = url;
    this.info =
        YoutubeProvider.getYoutubeDownloader()
            .getPlaylistInfo(
                new RequestPlaylistInfo(
                    MediaExtractionUtils.getYoutubeID(url)
                        .orElseThrow(() -> new UnknownPlaylistException(url))))
            .data();
    this.details = this.info.details();
  }

  @Override
  public @NotNull String getAuthor() {
    return this.details.author();
  }

  @Override
  public int getVideoCount() {
    return this.details.videoCount();
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull String getName() {
    return this.details.title();
  }

  @Override
  public @NotNull Collection<Video> getVideos() {
    return this.info.videos().parallelStream()
        .map(url -> new YoutubeVideo(url.videoId()))
        .collect(Collectors.toList());
  }

  @Override
  public long getViewCount() {
    return this.details.viewCount();
  }

  protected @NotNull PlaylistInfo getPlaylistInfo() {
    return this.info;
  }

  protected @NotNull PlaylistDetails getPlaylistDetails() {
    return this.details;
  }
}
