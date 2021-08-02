package io.github.pulsebeat02.ezmediacore.utility;

import org.jetbrains.annotations.NotNull;

public final class Tuple<A, B, C> {

  private final A a;
  private final B b;
  private final C c;

  public Tuple(@NotNull final A a, @NotNull final B b, @NotNull final C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public A getA() {
    return this.a;
  }

  public B getB() {
    return this.b;
  }

  public C getC() {
    return this.c;
  }
}
