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

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import com.google.gson.annotations.SerializedName;
import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherSetting;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioOutputType;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.PlaybackType;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public final class VideoCommandAttributes {

  public static final boolean TEMPORARY_PLACEHOLDER;

  static {
    TEMPORARY_PLACEHOLDER = true;
  }

  private transient AtomicBoolean completion;

  @SerializedName(value = "dither-type")
  private DitherSetting ditherType;

  @SerializedName(value = "audio-output")
  private AudioOutputType audioOutputType;

  @SerializedName(value = "playback-output")
  private PlaybackType playbackType;

  @SerializedName(value = "map-id")
  private int map;

  @SerializedName(value = "frame-width")
  private int frameWidth;

  @SerializedName(value = "frame-height")
  private int frameHeight;

  @SerializedName(value = "pixel-width")
  private int pixelWidth;

  @SerializedName(value = "pixel-height")
  private int pixelHeight;

  private transient VideoPlayer player;
  private transient MrlConfiguration videoMrl;
  private transient MrlConfiguration oggMrl;
  private transient EnhancedExecution extractor;
  private transient EnhancedExecution streamExtractor;
  private transient String resourcepackUrl; // for resourcepack url
  private transient byte[] resourcepackHash;

  public DitherSetting getDitherType() {
    return this.ditherType;
  }

  public void setDitherType(final DitherSetting ditherType) {
    this.ditherType = ditherType;
  }

  public VideoPlayer getPlayer() {
    return this.player;
  }

  public void setPlayer(final VideoPlayer player) {
    this.player = player;
  }

  public int getFrameWidth() {
    return this.frameWidth;
  }

  public void setFrameWidth(final int frameWidth) {
    this.frameWidth = frameWidth;
  }

  public int getFrameHeight() {
    return this.frameHeight;
  }

  public void setFrameHeight(final int frameHeight) {
    this.frameHeight = frameHeight;
  }

  public int getPixelWidth() {
    return this.pixelWidth;
  }

  public void setPixelWidth(final int pixelWidth) {
    this.pixelWidth = pixelWidth;
  }

  public int getPixelHeight() {
    return this.pixelHeight;
  }

  public void setPixelHeight(final int pixelHeight) {
    this.pixelHeight = pixelHeight;
  }

  public int getMap() {
    return this.map;
  }

  public void setMap(final int map) {
    this.map = map;
  }

  public @NotNull AtomicBoolean getCompletion() {
    return this.completion;
  }

  public @NotNull PlaybackType getVideoType() {
    return this.playbackType;
  }

  public void setVideoType(final PlaybackType type) {
    this.playbackType = type;
  }

  public String getPackUrl() {
    return this.resourcepackUrl;
  }

  public void setPackUrl(final String resourcepackUrl) {
    this.resourcepackUrl = resourcepackUrl;
  }

  public byte[] getPackHash() {
    return this.resourcepackHash;
  }

  public void setPackHash(final byte[] resourcepackHash) {
    this.resourcepackHash = resourcepackHash;
  }

  public EnhancedExecution getExtractor() {
    return this.extractor;
  }

  public void setExtractor(final EnhancedExecution extractor) {
    this.extractor = extractor;
  }

  public AudioOutputType getAudioOutputType() {
    return this.audioOutputType;
  }

  public void setAudioOutputType(final AudioOutputType audioOutputType) {
    this.audioOutputType = audioOutputType;
  }

  public MrlConfiguration getVideoMrl() {
    return this.videoMrl;
  }

  public void setVideoMrl(final MrlConfiguration videoMrl) {
    this.videoMrl = videoMrl;
  }

  public MrlConfiguration getOggMrl() {
    return this.oggMrl;
  }

  public void setOggMrl(final MrlConfiguration oggMrl) {
    this.oggMrl = oggMrl;
  }

  public EnhancedExecution getStreamExtractor() {
    return this.streamExtractor;
  }

  public void setStreamExtractor(final EnhancedExecution streamExtractor) {
    this.streamExtractor = streamExtractor;
  }

  public void cancelCurrentStream() {
    if (this.streamExtractor != null) {
      try {
        this.streamExtractor.close();
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }
}
