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

package io.github.pulsebeat02.deluxemediaplugin.utility;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

public final class ChatUtils {

  private static final ComponentLike PREFIX;
  private static final ComponentLike EXTERNAL_PROCESS;

  static {
    PREFIX =
        text()
            .color(AQUA)
            .append(
                text('['), text("DeluxeMediaPlugin", GOLD), text(']'), space(), text("»", GRAY));
    EXTERNAL_PROCESS =
        text()
            .color(AQUA)
            .append(text('['), text("External Process", GOLD), text(']'), space(), text("»", GRAY));
  }

  private ChatUtils() {}

  public static @NotNull Component format(@NotNull final Component message) {
    return join(separator(space()), PREFIX, message);
  }

  public static @NotNull Component ffmpeg(@NotNull final Component message) {
    return join(separator(space()), EXTERNAL_PROCESS, message);
  }

  public static @NotNull Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String[] dims = str.split(":");
    final String message;
    final OptionalInt width = ChatUtils.checkIntegerValidity(dims[0]);
    final OptionalInt height = ChatUtils.checkIntegerValidity(dims[1]);
    if (width.isEmpty()) {
      message = dims[0];
    } else if (height.isEmpty()) {
      message = dims[1];
    } else {
      return Optional.of(new int[] {width.getAsInt(), height.getAsInt()});
    }
    sender.sendMessage(
        text()
            .color(RED)
            .append(text("Argument '"))
            .append(text(str, GOLD))
            .append(text("' "))
            .append(text(message))
            .append(text(" is not a valid argument!"))
            .append(text(" (Must be Integer)")));
    return Optional.empty();
  }

  public static @NotNull OptionalInt checkIntegerValidity(@NotNull final String num) {
    try {
      return OptionalInt.of(Integer.parseInt(num));
    } catch (final NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  public static @NotNull TextComponent getCommandUsage(@NotNull final Map<String, String> usages) {
    final TextComponent.Builder builder =
        text().append(text("------------------", AQUA)).append(newline());
    for (final Map.Entry<String, String> entry : usages.entrySet()) {
      builder.append(
          join(
              separator(space()),
              text(entry.getKey(), LIGHT_PURPLE),
              text("-", GOLD),
              text(entry.getValue(), AQUA),
              newline()));
    }
    builder.append(text("------------------", AQUA));
    return builder.build();
  }

  public static void gold(@NotNull final Audience audience, final String message) {
    audience.sendMessage(format(text(message, GOLD)));
  }

  public static void red(@NotNull final Audience audience, final String message) {
    audience.sendMessage(format(text(message, RED)));
  }

  public static void aqua(@NotNull final Audience audience, final String message) {
    audience.sendMessage(format(text(message, AQUA)));
  }

  public static void external(@NotNull final Audience audience, @NotNull final String message) {
    audience.sendMessage(join(separator(space()), EXTERNAL_PROCESS, text(message, GOLD)));
  }
}
