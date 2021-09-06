package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.playlist.spotify.TrackDownloader;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.Cancellable;
import org.jetbrains.annotations.NotNull;

public interface SpotifyAudioExtractor extends EnhancedExecution, Cancellable {

  void onStartAudioExtraction();

  void onFinishAudioExtraction();

  @NotNull
  TrackDownloader getTrackDownloader();

  @NotNull
  YoutubeAudioExtractor getYoutubeExtractor();

}
