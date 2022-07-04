package io.github.pulsebeat02.ezmediacore.callback.audio;

@FunctionalInterface
public interface DiscordAudioConsumer {

  void consume(byte[] data);
}
