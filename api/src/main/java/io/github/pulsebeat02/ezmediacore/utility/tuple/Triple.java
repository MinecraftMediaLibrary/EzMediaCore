package io.github.pulsebeat02.ezmediacore.utility.tuple;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Triple<X, Y, Z> {

  private final X x;
  private final Y y;
  private final Z z;

  Triple(@Nullable final X x, @Nullable final Y y, @Nullable final Z z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Contract(value = "_, _, _ -> new", pure = true)
  public static <X, Y, Z> @NotNull Triple<X, Y, Z> ofTriple(
      @Nullable final X x, @Nullable final Y y, @Nullable final Z z) {
    return new Triple<>(x, y, z);
  }

  public @Nullable X getX() {
    return this.x;
  }

  public @Nullable Y getY() {
    return this.y;
  }

  public @Nullable final Z getZ() {
    return this.z;
  }
}
