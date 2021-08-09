package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.QuerySearch;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyTrack;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.Track;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.io.IOException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyQuerySearch implements QuerySearch {

  private final Track track;
  private final YoutubeVideo video;
  private final String query;

  public SpotifyQuerySearch(@NotNull final Track track) throws IOException {
    this.track = track;
    final String query = "%s %s".formatted(track.getName(), track.getArtists().get(0));
    this.video = new YoutubeVideo(MediaExtractionUtils.getFirstResultVideo(
        query).orElseThrow(
        () -> new DeadResourceLinkException(track.getId())));
    this.query = query;
  }

  public SpotifyQuerySearch(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    this(new SpotifyTrack(url));
  }

  @Override
  public @NotNull Video getYoutubeVideo() {
    return this.video;
  }

  @Override
  public @NotNull String getSearchQuery() {
    return this.query;
  }

  @Override
  public @NotNull Track getTrack() {
    return this.track;
  }

  YoutubeVideo getRawVideo() {
    return this.video;
  }
}
