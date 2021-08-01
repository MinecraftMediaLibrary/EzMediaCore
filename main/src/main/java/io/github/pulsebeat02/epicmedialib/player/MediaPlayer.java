package io.github.pulsebeat02.epicmedialib.player;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.callback.Callback;
import io.github.pulsebeat02.epicmedialib.callback.FrameCallback;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class MediaPlayer implements VideoPlayer {

  private final MediaLibraryCore core;
  private final FrameCallback callback;
  private final ImmutableDimension dimensions;
  private final Set<Player> watchers;
  private final String soundKey;
  private final String url;
  private final int frameRate;
  private PlayerControls controls;

  public MediaPlayer(
      @NotNull final MediaLibraryCore core,
      @NotNull final FrameCallback callback,
      @NotNull final ImmutableDimension dimensions,
      @NotNull final String url,
      final int frameRate) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be empty or null!");
    Preconditions.checkArgument(dimensions.getWidth() >= 0, "Width must be above or equal to 0!");
    Preconditions.checkArgument(dimensions.getHeight() >= 0, "Height must be above or equal to 0!");
    this.core = core;
    this.callback = callback;
    this.dimensions = dimensions;
    this.soundKey = core.getPlugin().getName().toLowerCase(Locale.ROOT);
    this.url = url;
    this.frameRate = frameRate;
    this.watchers = Collections.newSetFromMap(new WeakHashMap<>());
    this.watchers.addAll(
        Arrays.stream(callback.getViewers()).map(Bukkit::getPlayer).collect(Collectors.toSet()));
  }

  @Override
  public @NotNull Callback getCallback() {
    return this.callback;
  }

  @Override
  public @NotNull String getSoundKey() {
    return this.soundKey;
  }

  @Override
  public @NotNull PlayerControls getPlayerState() {
    return this.controls;
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    onPlayerStateChange(controls);
    this.controls = controls;
  }

  @Override
  public void onPlayerStateChange(@NotNull final PlayerControls status) {}

  @Override
  public void playAudio() {
    for (final Player player : this.watchers) {
      player.playSound(player.getLocation(), this.soundKey, SoundCategory.MUSIC, 100.0F, 1.0F);
    }
  }

  @Override
  public void stopAudio() {
    for (final Player player : this.watchers) {
      player.stopSound(this.soundKey, SoundCategory.MUSIC);
    }
  }

  @Override
  public int getFrameRate() {
    return this.frameRate;
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull ImmutableDimension getDimensions() {
    return this.dimensions;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull Set<Player> getWatchers() {
    return this.watchers;
  }
}
