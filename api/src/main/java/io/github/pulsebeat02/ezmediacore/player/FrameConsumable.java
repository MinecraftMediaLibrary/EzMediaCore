package io.github.pulsebeat02.ezmediacore.player;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface FrameConsumable {

  void setCustomVideoAdapter(@NotNull final Consumer<int[]> pixels);
}
