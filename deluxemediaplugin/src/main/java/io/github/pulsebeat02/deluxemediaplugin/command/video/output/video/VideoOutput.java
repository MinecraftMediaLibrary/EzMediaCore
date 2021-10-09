package io.github.pulsebeat02.deluxemediaplugin.command.video.output.video;

import org.jetbrains.annotations.NotNull;

public abstract class VideoOutput implements PlaybackOutputHandle {

  private final String name;

  public VideoOutput(@NotNull final String name) {
    this.name = name;
  }

  @Override
  public @NotNull String getName() {
    return this.name;
  }
}
