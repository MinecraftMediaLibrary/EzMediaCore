package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimensional;
import io.github.pulsebeat02.ezmediacore.playlist.ResourceUrl;
import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface VideoPlayer extends LibraryInjectable, ResourceUrl, Dimensional {

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

  long getElapsedMilliseconds();
}
