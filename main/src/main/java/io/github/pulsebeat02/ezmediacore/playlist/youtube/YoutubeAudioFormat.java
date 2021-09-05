/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

public class YoutubeAudioFormat implements AudioFormat {

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

  private final com.github.kiulian.downloader.model.videos.formats.AudioFormat format;

  public YoutubeAudioFormat(
      @NotNull final com.github.kiulian.downloader.model.videos.formats.AudioFormat format) {
    this.format = format;
  }

  static @NotNull BiMap<
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
