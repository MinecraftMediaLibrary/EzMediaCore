package io.github.pulsebeat02.deluxemediaplugin.utility.mutable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MutableLong {

  private long number;

  MutableLong(final long number) {
    this.number = number;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableLong ofNumber(@NotNull final Number number) {
    return ofLong(number.longValue());
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableLong ofLong(final long number) {
    return new MutableLong(number);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableLong ofString(@NotNull final String string) {
    return ofLong(Long.parseLong(string));
  }

  public void increment() {
    this.number++;
  }

  public void decrement() {
    this.number--;
  }

  public void add(final long add) {
    this.number += add;
  }

  public void subtract(final long subtract) {
    this.number -= subtract;
  }

  public void set(final long newNumber) {
    this.number = newNumber;
  }

  public long getNumber() {
    return this.number;
  }
}
