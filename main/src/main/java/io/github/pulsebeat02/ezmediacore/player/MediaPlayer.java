package io.github.pulsebeat02.ezmediacore.player;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
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
import org.jetbrains.annotations.Nullable;

public abstract class MediaPlayer implements VideoPlayer {

  private final MediaLibraryCore core;
  private final FrameCallback callback;
  private final Dimension dimensions;
  private final Set<Player> watchers;
  private final String key;
  private final String url;
  private final int fps;
  private PlayerControls controls;

  MediaPlayer(
      @NotNull final FrameCallback callback,
      @NotNull final Dimension pixelDimension,
      @NotNull final String url,
      @Nullable final String key,
      final int fps) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be empty or null!");
    Preconditions.checkArgument(pixelDimension.getWidth() >= 0,
        "Width must be above or equal to 0!");
    Preconditions.checkArgument(pixelDimension.getHeight() >= 0,
        "Height must be above or equal to 0!");
    this.core = callback.getCore();
    this.callback = callback;
    this.dimensions = pixelDimension;
    this.key =
        key == null ? callback.getCore().getPlugin().getName().toLowerCase(Locale.ROOT) : key;
    this.url = url;
    this.fps = fps;
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
    return this.key;
  }

  @Override
  public @NotNull PlayerControls getPlayerState() {
    return this.controls;
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    this.onPlayerStateChange(controls);
    this.controls = controls;
    this.callback.preparePlayerStateChange(controls);
  }

  @Override
  public void onPlayerStateChange(@NotNull final PlayerControls status) {
  }

  @Override
  public void playAudio() {
    for (final Player player : this.watchers) {
      player.playSound(player.getLocation(), this.key, SoundCategory.MASTER, 100.0F, 1.0F);
    }
  }

  @Override
  public void stopAudio() {
    for (final Player player : this.watchers) {
      player.stopSound(this.key, SoundCategory.MASTER);
    }
  }

  @Override
  public int getFrameRate() {
    return this.fps;
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull Dimension getDimensions() {
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
