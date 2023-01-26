package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public interface InputParser extends LibraryInjectable {

  @NotNull
  Pair<Object, String[]> parseInput(@NotNull Input input);

  @NotNull
  Pair<Object, String[]> parseUrl(@NotNull Input input);

  @NotNull
  Pair<Object, String[]> parsePath(@NotNull Input input);

  @NotNull
  Pair<Object, String[]> parseDevice(@NotNull Input input);

  @NotNull
  Pair<Object, String[]> parseMrl(@NotNull Input input);

  @NotNull
  Pair<Object, String[]> parseDesktop(@NotNull Input input);

  @NotNull
  Pair<Object, String[]> parseWindow(@NotNull Input input);
}
