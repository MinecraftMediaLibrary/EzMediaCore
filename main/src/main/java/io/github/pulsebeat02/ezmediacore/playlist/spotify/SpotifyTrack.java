package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingFunction;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyTrack implements Track {

  private final com.wrapper.spotify.model_objects.specification.Track track;
  private final String url;
  private final List<Artist> artists;

  public SpotifyTrack(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    this.url = url;
    this.track =
        SpotifyProvider.getSpotifyApi()
            .getTrack(
                MediaExtractionUtils.getSpotifyID(url)
                    .orElseThrow(() -> new DeadResourceLinkException(url)))
            .build()
            .execute();
    this.artists =
        Arrays.stream(this.track.getArtists())
            .map(ThrowingFunction.unchecked(SpotifyArtist::new))
            .collect(Collectors.toList());
  }

  @Override
  public @NotNull String getId() {
    return this.track.getId();
  }

  @Override
  public @NotNull String getName() {
    return this.track.getName();
  }

  @Override
  public @NotNull List<Artist> getArtists() {
    return this.artists;
  }

  @Override
  public @NotNull Map<String, String> getExternalUrls() {
    return this.track.getExternalUrls().getExternalUrls();
  }

  @Override
  public @NotNull String getPreviewUrl() {
    return this.track.getPreviewUrl();
  }

  @Override
  public int getDiscNumber() {
    return this.track.getDiscNumber();
  }

  @Override
  public int getDuration() {
    return this.track.getDurationMs();
  }

  @Override
  public boolean isExplicit() {
    return this.track.getIsExplicit();
  }

  @Override
  public boolean isPlayable() {
    return this.track.getIsPlayable();
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  protected @NotNull com.wrapper.spotify.model_objects.specification.Track getTrack() {
    return this.track;
  }
}
