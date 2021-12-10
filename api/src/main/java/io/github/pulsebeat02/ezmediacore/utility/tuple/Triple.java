package io.github.pulsebeat02.ezmediacore.utility.tuple;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Triple<X, Y, Z> {

  private final X x;
  private final Y y;
  private final Z z;

  Triple(@NotNull final X x, @NotNull final Y y, @NotNull final Z z) {
    checkNotNull(x, "First value cannot be null!");
    checkNotNull(y, "Second value cannot be null!");
    checkNotNull(z, "Third value cannot be null!");
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Contract(value = "_, _, _ -> new", pure = true)
  public static <X, Y, Z> @NotNull Triple<X, Y, Z> ofTriple(
      @NotNull final X x, @NotNull final Y y, @NotNull final Z z) {
    return new Triple<>(x, y, z);
  }

  public @NotNull X getX() {
    return this.x;
  }

  public @NotNull Y getY() {
    return this.y;
  }

  public @NotNull final Z getZ() {
    return this.z;
  }
}
