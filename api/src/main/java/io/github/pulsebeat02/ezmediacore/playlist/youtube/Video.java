package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import io.github.pulsebeat02.ezmediacore.playlist.Identifier;
import io.github.pulsebeat02.ezmediacore.playlist.ResourceUrl;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface Video extends ResourceUrl, Identifier {

  @NotNull
  String getLiveUrl();

  @NotNull
  List<String> getKeywords();

  @NotNull
  List<VideoFormat> getVideoFormats();

  @NotNull
  List<AudioFormat> getAudioFormats();

  long getViewCount();

  int getAverageRating();

  boolean isLiveContent();
}
