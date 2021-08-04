package io.github.pulsebeat02.ezmediacore.player;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.EntityCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class VLCEntityMediaPlayer extends VLCMediaPlayer {

  private final Entity[] entities;

  protected VLCEntityMediaPlayer(
      @NotNull final MediaLibraryCore core,
      @NotNull final FrameCallback callback,
      @NotNull final ImmutableDimension dimensions,
      @NotNull final String url,
      final int frameRate) {
    super(core, callback, dimensions, url, frameRate);
    Preconditions.checkArgument(
        callback instanceof EntityCallbackDispatcher,
        "Callback not instance of EntityCallbackDispatcher!");
    this.entities = ((EntityCallbackDispatcher) callback).getEntities();
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    switch (controls) {
      case START:
      case RELEASE:
        this.removeEntities();
        break;
    }
    super.setPlayerState(controls);
  }

  private void removeEntities() {
    if (this.entities != null) {
      for (final Entity entity : this.entities) {
        entity.remove();
      }
    }
  }
}
