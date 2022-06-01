package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;

import com.google.gson.annotations.SerializedName;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.DitheringAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.PlayerAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.VideoPlayback;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioPlayback;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScreenConfig {

  private final transient AtomicBoolean completion;
  private transient VideoPlayer player;
  private transient Input media;
  private transient Input ogg;
  private transient EnhancedExecution extractor;
  private transient EnhancedExecution stream;
  private transient CompletableFuture<Object> task;
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

  {
    this.completion = new AtomicBoolean(false);
  }

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

  public boolean getCompletion() {
    return this.completion.get();
  }

  public void setCompletion(final boolean status) {
    this.completion.set(status);
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
    return handleNull(audience, Locale.ERR_VIDEO_NOT_LOADED.build(), this.media);
  }

  public boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    return handleFalse(audience, Locale.ERR_VIDEO_PROCESSING.build(), this.completion.get());
  }

  public boolean mediaUninitialized(@NotNull final Audience audience) {
    return handleNull(audience, Locale.ERR_VIDEO_NOT_LOADED.build(), this.player);
  }

  public @Nullable CompletableFuture<Object> getTask() {
    return this.task;
  }

  public void setTask(final CompletableFuture<Object> task) {
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
}
