package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public final class VLCMediaPlayerInputParser extends MediaPlayerInputParser {

  public VLCMediaPlayerInputParser(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseUrl(@NotNull final UrlInput input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parsePath(@NotNull final PathInput input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDevice(@NotNull final DeviceInput input) {
    throw new UnsupportedOperationException(
        "Device input device not supported for VLC Media Player!");
  }

  @Override
  public @NotNull Pair<Object, String[]> parseMrl(@NotNull final MrlInput input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDesktop(@NotNull final DesktopInput input) {
    // -I dummy screen://
    // Arguments: -I dummy
    // URL to Play: screen://
    return Pair.ofPair("screen://", new String[] {"-I", "dummy"});
  }

  @Override
  public @NotNull Pair<Object, String[]> parseWindow(@NotNull final WindowInput input) {
    throw new UnsupportedOperationException(
        "Window input device not supported for VLC Media Player!");
  }
}
