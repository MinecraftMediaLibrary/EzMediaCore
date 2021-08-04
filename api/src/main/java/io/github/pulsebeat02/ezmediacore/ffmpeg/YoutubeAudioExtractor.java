package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoDownloader;
import org.jetbrains.annotations.NotNull;

public interface YoutubeAudioExtractor extends EnhancedExecution {

  void onStartAudioExtraction();

  void onFinishAudioExtraction();

  @NotNull
  VideoDownloader getDownloader();

  @NotNull
  AudioExtractor getExtractor();
}
