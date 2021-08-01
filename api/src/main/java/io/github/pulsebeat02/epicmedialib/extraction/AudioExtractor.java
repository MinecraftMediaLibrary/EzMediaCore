package io.github.pulsebeat02.epicmedialib.extraction;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface AudioExtractor {

  @NotNull
  Path extractAudio();

  void onStartAudioExtraction();

  void onFinishAudioExtraction();
}
