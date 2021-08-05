package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import static com.github.kiulian.downloader.model.videos.quality.AudioQuality.high;
import static com.github.kiulian.downloader.model.videos.quality.AudioQuality.low;
import static com.github.kiulian.downloader.model.videos.quality.AudioQuality.medium;
import static com.github.kiulian.downloader.model.videos.quality.AudioQuality.noAudio;
import static com.github.kiulian.downloader.model.videos.quality.AudioQuality.unknown;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.AudioQuality.HIGH;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.AudioQuality.LOW;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.AudioQuality.MEDIUM;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.AudioQuality.NOAUDIO;
import static io.github.pulsebeat02.ezmediacore.playlist.youtube.AudioQuality.UNKNOWN;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public record YoutubeAudioFormat(
    com.github.kiulian.downloader.model.videos.formats.AudioFormat format) implements AudioFormat {

  private static final BiMap<
      com.github.kiulian.downloader.model.videos.quality.AudioQuality, AudioQuality>
      AUDIO_FORMATS;

  static {
    AUDIO_FORMATS =
        HashBiMap.create(
            Map.of(
                unknown, UNKNOWN,
                noAudio, NOAUDIO,
                low, LOW,
                medium, MEDIUM,
                high, HIGH));
  }

  public YoutubeAudioFormat(
      @NotNull final com.github.kiulian.downloader.model.videos.formats.AudioFormat format) {
    this.format = format;
  }

  static @NotNull
  BiMap<
      com.github.kiulian.downloader.model.videos.quality.AudioQuality, AudioQuality>
  getAudioMappings() {
    return AUDIO_FORMATS;
  }

  @Override
  public int getBitrate() {
    return this.format.averageBitrate();
  }

  @Override
  public int getSamplingRate() {
    return this.format.audioSampleRate();
  }

  @Override
  public @NotNull
  AudioQuality getAudioQuality() {
    return AUDIO_FORMATS.get(this.format.audioQuality());
  }
}
