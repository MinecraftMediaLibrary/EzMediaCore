/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.utils.immutable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A shallowly immutable pair of two non-null values, retained by reference rather than copied.
 *
 * <p>This class is immutable, and thread-safe when its values are.
 *
 * @param <FirstType>  the type of the first value
 * @param <SecondType> the type of the second value
 */
public final class Pair<FirstType, SecondType> {

  private final FirstType first;
  private final SecondType second;

  Pair(final FirstType first, final SecondType second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Creates a pair.
   *
   * @param <FirstType>  the type of the first value
   * @param <SecondType> the type of the second value
   * @param first        the first value
   * @param second       the second value
   * @return the pair
   * @throws NullPointerException if {@code first} or {@code second} is null
   */
  public static <FirstType, SecondType> Pair<FirstType, SecondType> pair(final FirstType first, final SecondType second) {
    Preconditions.checkNotNull(first, "First value must not be null!");
    Preconditions.checkNotNull(second, "Second value must not be null!");

    return new Pair<>(first, second);
  }

  /**
   * Returns the first value.
   *
   * @return the first value
   */
  public FirstType getFirst() {
    return this.first;
  }

  /**
   * Returns the second value.
   *
   * @return the second value
   */
  public SecondType getSecond() {
    return this.second;
  }

  /**
   * Returns whether {@code other} is a pair with equal values.
   *
   * @param other the object to compare with
   * @return whether both match
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final Pair<?, ?> pair)) {
      return false;
    }

    return Objects.equals(this.first, pair.first) && Objects.equals(this.second, pair.second);
  }

  /**
   * Returns a hash code built from both values.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.first, this.second);
  }

  /**
   * Returns a debugging representation with both values.
   *
   * @return the representation
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("first", this.first).add("second", this.second).toString();
  }
}
