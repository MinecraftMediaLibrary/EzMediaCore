package io.github.pulsebeat02.ezmediacore.ffmpeg;

public interface YoutubeAudioExtractor extends EnhancedExecution {

  void onStartAudioExtraction();

  void onFinishAudioExtraction();
}
