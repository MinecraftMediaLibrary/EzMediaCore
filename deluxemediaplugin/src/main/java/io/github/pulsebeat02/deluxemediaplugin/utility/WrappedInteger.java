package io.github.pulsebeat02.deluxemediaplugin.utility;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class WrappedInteger {

  private int number;

  WrappedInteger(@NotNull final Number number) {
    this.number = (int) number;
  }

  WrappedInteger(final int number) {
    this.number = number;
  }

  WrappedInteger(@NotNull final String number) {
    this.number = Integer.parseInt(number);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull WrappedInteger ofNumber(@NotNull final Number number) {
    return new WrappedInteger(number);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull WrappedInteger ofInteger(final int number) {
    return new WrappedInteger(number);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull WrappedInteger ofString(@NotNull final String string) {
    return new WrappedInteger(string);
  }

  public void increment() {
    this.number++;
  }

  public void decrement() {
    this.number--;
  }

  public void add(final int add) {
    this.number += add;
  }

  public void subtract(final int subtract) {
    this.number -= subtract;
  }

  public void set(final int newNumber) {
    this.number = newNumber;
  }

  public int getNumber() {
    return this.number;
  }
}
