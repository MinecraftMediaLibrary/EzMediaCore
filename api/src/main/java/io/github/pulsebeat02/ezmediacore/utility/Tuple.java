package io.github.pulsebeat02.ezmediacore.utility;

import org.jetbrains.annotations.NotNull;

public record Tuple<A, B, C>(A a, B b, C c) {

  public Tuple(@NotNull final A a, @NotNull final B b, @NotNull final C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }
}
