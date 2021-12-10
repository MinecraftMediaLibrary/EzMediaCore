/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.player;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import org.bukkit.Sound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class SoundKey {

  private final String name;

  SoundKey(@NotNull final String name) {
    checkNotNull(name, "Sound key cannot be null!");
    this.name = name;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull SoundKey ofSound(@NotNull final String sound) {
    return new SoundKey(sound);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull SoundKey ofSound(@NotNull final Sound sound) {
    return ofSound(sound.getKey().getNamespace());
  }

  public @NotNull String getName() {
    return this.name;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{namespaced-key=%s}".formatted(this.name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof SoundKey)) {
      return false;
    }
    return ((SoundKey) obj).name.equals(this.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }
}
