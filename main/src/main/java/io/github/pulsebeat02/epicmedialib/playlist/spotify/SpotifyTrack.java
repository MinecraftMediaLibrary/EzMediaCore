package io.github.pulsebeat02.epicmedialib.playlist.spotify;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.epicmedialib.sneaky.ThrowingFunction;
import io.github.pulsebeat02.epicmedialib.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.epicmedialib.utility.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyTrack implements Track {

  private final com.wrapper.spotify.model_objects.specification.Track track;
  private final String url;

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
  public @NotNull Collection<Artist> getArtists() {
    return Arrays.stream(this.track.getArtists())
        .map(ThrowingFunction.unchecked(SpotifyArtist::new))
        .collect(Collectors.toList());
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
