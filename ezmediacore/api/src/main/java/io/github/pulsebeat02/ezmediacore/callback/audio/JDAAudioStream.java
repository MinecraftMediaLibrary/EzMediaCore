package io.github.pulsebeat02.ezmediacore.callback.audio;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public interface JDAAudioStream {
  boolean canProvide();

  @NotNull
  ByteBuffer provide20MsAudio();

  void pause();

  void play();
}
