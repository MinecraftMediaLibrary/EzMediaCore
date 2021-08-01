package io.github.pulsebeat02.epicmedialib.player;

import io.github.pulsebeat02.epicmedialib.LibraryInjectable;
import io.github.pulsebeat02.epicmedialib.callback.Callback;
import io.github.pulsebeat02.epicmedialib.playlist.ResourceUrl;
import io.github.pulsebeat02.epicmedialib.utility.Dimension;
import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface VideoPlayer extends LibraryInjectable, ResourceUrl, Dimension {

  @NotNull
  Callback getCallback();

  @NotNull
  String getSoundKey();

  @NotNull
  PlayerControls getPlayerState();

  void setPlayerState(@NotNull final PlayerControls controls);

  void onPlayerStateChange(@NotNull final PlayerControls status);

  void playAudio();

  void stopAudio();

  int getFrameRate();

  void initializePlayer(final long ms);

  @NotNull
  Set<Player> getWatchers();
}
