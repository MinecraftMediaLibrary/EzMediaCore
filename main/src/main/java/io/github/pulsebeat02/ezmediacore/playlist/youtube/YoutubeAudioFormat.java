package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

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

public final class YoutubeAudioFormat implements AudioFormat {

  private static final BiMap<
          com.github.kiulian.downloader.model.videos.quality.AudioQuality, AudioQuality>
      AUDIO_FORMATS;

  static {
    AUDIO_FORMATS =
        HashBiMap.create(
            ImmutableMap.of(
                unknown, UNKNOWN,
                noAudio, NOAUDIO,
                low, LOW,
                medium, MEDIUM,
                high, HIGH));
  }

  private final com.github.kiulian.downloader.model.videos.formats.AudioFormat format;

  YoutubeAudioFormat(
      @NotNull final com.github.kiulian.downloader.model.videos.formats.AudioFormat format) {
    this.format = format;
  }

  protected static @NotNull BiMap<
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
  public @NotNull AudioQuality getAudioQuality() {
    return AUDIO_FORMATS.get(this.format.audioQuality());
  }
}
