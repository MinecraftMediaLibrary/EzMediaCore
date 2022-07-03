package io.github.pulsebeat02.ezmediacore.callback;

@FunctionalInterface
public interface DiscordAudioConsumer {

  void consume(byte[] data);
}
