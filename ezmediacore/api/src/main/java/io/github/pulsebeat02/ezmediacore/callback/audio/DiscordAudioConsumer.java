package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DiscordAudioConsumer {

  void consume(@NotNull final BufferCarrier data);
}
