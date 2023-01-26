package io.github.pulsebeat02.ezmediacore.player.input;

import org.jetbrains.annotations.NotNull;

public final class PlayerInputHolder implements PlayerInput {

  private final Input video;
  private final Input audio;

  public PlayerInputHolder(@NotNull final Input video, @NotNull final Input audio) {
    this.video = video;
    this.audio = audio;
  }

  public static @NotNull PlayerInputHolder ofInputs(
      @NotNull final Input video, @NotNull final Input audio) {
    return new PlayerInputHolder(video, audio);
  }

  @Override
  public @NotNull Input getDirectVideoMrl() {
    return this.video;
  }

  @Override
  public @NotNull Input getDirectAudioMrl() {
    return this.audio;
  }
}
