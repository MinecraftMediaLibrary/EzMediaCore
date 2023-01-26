/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNonNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;

import com.google.gson.annotations.SerializedName;
import io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg.AudioPlayerStreamSendHandler;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.DitheringAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.PlayerAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.VideoPlayback;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioPlayback;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScreenConfig {

  private transient VideoPlayer player;
  private transient Input media;
  private transient Input ogg;
  private transient EnhancedExecution extractor;
  private transient EnhancedExecution stream;

  private transient AudioPlayerStreamSendHandler discordHandler;
  private transient CompletableFuture<Void> task;
  private transient String packUrl;
  private transient byte[] packHash;

  @SerializedName(value = "dithering-algorithm")
  private DitheringAlgorithm ditheringAlgorithm;

  @SerializedName(value = "native-dithering")
  private boolean nativeDithering = false;

  @SerializedName(value = "player-algorithm")
  private PlayerAlgorithm playerAlgorithm = PlayerAlgorithm.UNSPECIFIED;

  @SerializedName(value = "audio-playback")
  private AudioPlayback audioPlayback = AudioPlayback.RESOURCEPACK;

  @SerializedName(value = "video-playback")
  private VideoPlayback videoPlayback = VideoPlayback.ITEMFRAME;

  @SerializedName(value = "starting-map-id")
  private int map = 0;

  @SerializedName(value = "itemframe-width")
  private int itemframeWidth = 5;

  @SerializedName(value = "itemframe-height")
  private int itemframeHeight = 5;

  @SerializedName(value = "resolution-width")
  private int resolutionWidth = 640;

  @SerializedName(value = "resolution-height")
  private int resolutionHeight = 640;

  public @Nullable EnhancedExecution getStream() {
    return this.stream;
  }

  public void setStream(final EnhancedExecution stream) {
    this.stream = stream;
  }

  public @Nullable EnhancedExecution getExtractor() {
    return this.extractor;
  }

  public void setExtractor(final EnhancedExecution extractor) {
    this.extractor = extractor;
  }

  public @Nullable Input getOggMedia() {
    return this.ogg;
  }

  public @Nullable Input getMedia() {
    return this.media;
  }

  public void setMedia(final Input media) {
    this.media = media;
  }

  public @Nullable VideoPlayer getPlayer() {
    return this.player;
  }

  public void setPlayer(final VideoPlayer player) {
    this.player = player;
  }

  public byte @Nullable [] getPackHash() {
    return this.packHash;
  }

  public void setPackHash(final byte[] packHash) {
    this.packHash = packHash;
  }

  public @Nullable String getPackUrl() {
    return this.packUrl;
  }

  public void setPackUrl(final String packUrl) {
    this.packUrl = packUrl;
  }

  public @NotNull DitheringAlgorithm getDitheringAlgorithm() {
    return this.ditheringAlgorithm;
  }

  public void setDitheringAlgorithm(final DitheringAlgorithm algorithm) {
    this.ditheringAlgorithm = algorithm;
  }

  public @NotNull AudioPlayback getAudioPlayback() {
    return this.audioPlayback;
  }

  public void setAudioPlayback(@NotNull final AudioPlayback playback) {
    this.audioPlayback = playback;
  }

  public @NotNull VideoPlayback getVideoPlayback() {
    return this.videoPlayback;
  }

  public void setVideoPlayback(@NotNull final VideoPlayback playback) {
    this.videoPlayback = playback;
  }

  public int getStartingMap() {
    return this.map;
  }

  public int getItemframeHeight() {
    return this.itemframeHeight;
  }

  public void setItemframeHeight(final int height) {
    this.itemframeHeight = height;
  }

  public int getItemframeWidth() {
    return this.itemframeWidth;
  }

  public void setItemframeWidth(final int width) {
    this.itemframeWidth = width;
  }

  public int getResolutionWidth() {
    return this.resolutionWidth;
  }

  public void setResolutionWidth(final int width) {
    this.resolutionWidth = width;
  }

  public int getResolutionHeight() {
    return this.resolutionHeight;
  }

  public void setResolutionHeight(final int height) {
    this.resolutionHeight = height;
  }

  public boolean mediaNotSpecified(@NotNull final Audience audience) {
    return handleNull(audience, Locale.UNLOADED_VIDEO.build(), this.media);
  }

  public boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    return handleNonNull(audience, Locale.PROCESSING_VIDEO.build(), this.task);
  }

  public boolean mediaUninitialized(@NotNull final Audience audience) {
    return handleNull(audience, Locale.UNLOADED_VIDEO.build(), this.player);
  }

  public @Nullable CompletableFuture<Void> getTask() {
    return this.task;
  }

  public void setTask(final CompletableFuture<Void> task) {
    this.task = task;
  }

  public void setOgg(final Input ogg) {
    this.ogg = ogg;
  }

  public PlayerAlgorithm getPlayerAlgorithm() {
    return this.playerAlgorithm;
  }

  public void setPlayerAlgorithm(@NotNull final PlayerAlgorithm algorithm) {
    this.playerAlgorithm = algorithm;
  }

  public void setDitherMap(final int map) {
    this.map = map;
  }

  public boolean getNativeDithering() {
    return this.nativeDithering;
  }

  public void setNativeDithering(final boolean useNative) {
    this.nativeDithering = useNative;
  }

  public @Nullable AudioPlayerStreamSendHandler getDiscordHandler() {
    return this.discordHandler;
  }

  public void setDiscordHandler(@NotNull final AudioPlayerStreamSendHandler discordHandler) {
    this.discordHandler = discordHandler;
  }
}
