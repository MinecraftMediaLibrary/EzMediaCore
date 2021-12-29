package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public interface InputParser extends LibraryInjectable {

  @NotNull Pair<Object, String[]> parseInput(@NotNull InputItem input);

  @NotNull Pair<Object, String[]> parseUrl(@NotNull UrlInput input);

  @NotNull Pair<Object, String[]> parsePath(@NotNull PathInput input);

  @NotNull Pair<Object, String[]> parseDevice(@NotNull DeviceInput input);

  @NotNull Pair<Object, String[]> parseMrl(@NotNull MrlInput input);

  @NotNull Pair<Object, String[]> parseDesktop(@NotNull DesktopInput input);

  @NotNull Pair<Object, String[]> parseWindow(@NotNull WindowInput input);
}
