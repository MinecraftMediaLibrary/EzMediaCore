package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VLCMediaFrame implements VLCFrame{

  private final int[] rgb;
  private final byte[] samples;

  VLCMediaFrame(final int @Nullable [] rgb, final byte @Nullable [] samples) {
    this.rgb = rgb;
    this.samples = samples;
  }

  @Contract("_, _ -> new")
  public static @NotNull VLCMediaFrame ofFrame(final int @Nullable [] rgb, final byte @Nullable [] samples) {
    return new VLCMediaFrame(rgb, samples);
  }

  @Override
  public @NotNull Optional<int[]> getRGBSamples() {
    return Optional.ofNullable(this.rgb);
  }

  @Override
  public @NotNull Optional<byte[]> getAudioSamples() {
    return Optional.ofNullable(this.samples);
  }
}
