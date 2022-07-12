package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DiscordCallback extends SampleCallback {

  private final DiscordAudioConsumer consumer;

  public DiscordCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final DiscordAudioConsumer consumer) {
    super(core, viewers);
    this.consumer = consumer;
  }

  @Override
  public void process(final byte @NotNull [] data) {
    this.consumer.consume(ByteBufCarrier.ofByteArray(data));
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {}

  public static final class Builder extends AudioCallbackBuilder {

    private DiscordAudioConsumer consumer;

    Builder() {}

    @Contract("_ -> this")
    public Builder consumer(@NotNull final DiscordAudioConsumer consumer) {
      this.consumer = consumer;
      return this;
    }

    @Override
    public @NotNull DiscordCallback build(@NotNull final MediaLibraryCore core) {
      final Viewers viewers = this.getViewers();
      return new DiscordCallback(core, viewers, this.consumer);
    }
  }
}
