package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public final class VLCMediaPlayerInputParser extends MediaPlayerInputParser {

  public VLCMediaPlayerInputParser(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseUrl(@NotNull final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parsePath(@NotNull final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDevice(@NotNull final Input input) {
    throw new UnsupportedOperationException(
        "Device input device not supported for VLC Media Player!");
  }

  @Override
  public @NotNull Pair<Object, String[]> parseMrl(@NotNull final Input input) {
    return Pair.ofPair(input.getInput(), EMPTY_ARGS);
  }

  @Override
  public @NotNull Pair<Object, String[]> parseDesktop(@NotNull final Input input) {
    // -I dummy screen://
    // Arguments: -I dummy
    // URL to Play: screen://
    return Pair.ofPair("screen://", new String[] {"-I", "dummy"});
  }

  @Override
  public @NotNull Pair<Object, String[]> parseWindow(@NotNull final Input input) {
    throw new UnsupportedOperationException(
        "Window input device not supported for VLC Media Player!");
  }
}
