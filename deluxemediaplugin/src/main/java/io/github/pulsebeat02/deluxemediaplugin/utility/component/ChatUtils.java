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

package io.github.pulsebeat02.deluxemediaplugin.utility.component;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

public final class ChatUtils {

  private ChatUtils() {}

  public static @NotNull Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {

    final String[] dims = str.split(":");
    final OptionalInt width = parseInt(dims[0]);
    final OptionalInt height = parseInt(dims[1]);

    if (width.isPresent() && height.isPresent()) {
      return Optional.of(new int[] {width.getAsInt(), height.getAsInt()});
    }

    sender.sendMessage(Locale.ERR_INVALID_DIMS.build());

    return Optional.empty();
  }

  public static @NotNull OptionalInt parseInt(@NotNull final String num) {
    try {
      return OptionalInt.of(Integer.parseInt(num));
    } catch (final NumberFormatException e) {
      return OptionalInt.empty();
    }
  }
}
