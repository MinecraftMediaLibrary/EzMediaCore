package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

/** Yes I know. This is why you shouldn't use it. JCodec sucks. */
public final class JCodecMediaPlayerInputParser extends MediaPlayerInputParser {

  public JCodecMediaPlayerInputParser(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseUrl(@NotNull final UrlInput input) {
    throw new UnsupportedOperationException("URL input not supported for JCodec Media Player!");
  }

  @Override
  public @NotNull Pair<Object, String[]> parsePath(@NotNull final PathInput input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDevice(@NotNull final DeviceInput input) {
    throw new UnsupportedOperationException(
        "Device input device not supported for JCodec Media Player!");
  }

  @Override
  public @NotNull Pair<Object, String[]> parseMrl(@NotNull final MrlInput input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDesktop(@NotNull final DesktopInput input) {
    throw new UnsupportedOperationException("Desktop input not supported for JCodec Media Player!");
  }

  @Override
  public @NotNull Pair<Object, String[]> parseWindow(@NotNull final WindowInput input) {
    throw new UnsupportedOperationException(
        "Window input device not supported for JCodec Media Player!");
  }
}
