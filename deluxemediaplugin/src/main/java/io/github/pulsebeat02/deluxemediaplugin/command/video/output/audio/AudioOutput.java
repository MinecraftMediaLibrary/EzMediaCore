package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import org.jetbrains.annotations.NotNull;

public abstract class AudioOutput implements AudioOutputHandle {

  private final String name;

  public AudioOutput(@NotNull final String name) {
    this.name = name;
  }

  @Override
  public @NotNull String getName() {
    return this.name;
  }
}
