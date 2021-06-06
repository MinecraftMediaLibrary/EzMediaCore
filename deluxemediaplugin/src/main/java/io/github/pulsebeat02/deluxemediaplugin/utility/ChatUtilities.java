/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.utility;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class ChatUtilities {

  private static final ComponentLike PREFIX;

  static {
    PREFIX =
        Component.text()
            .color(NamedTextColor.AQUA)
            .append(
                Component.text('['),
                Component.text("DeluxeMediaPlugin", NamedTextColor.GOLD),
                Component.text(']'));
  }

  public static Component format(@NotNull final TextComponent message) {
    return TextComponent.ofChildren(PREFIX, Component.space(), message);
  }

  public static OptionalLong checkMapBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String message;
    final OptionalLong opt = checkLongValidity(str);
    if (!opt.isPresent()) {
      message = "is not a valid argument!";
    } else {
      final long id = opt.getAsLong();
      if (id < -2_147_483_647L) {
        message = "is too low!";
      } else if (id > 2_147_483_647L) {
        message = "is too high!";
      } else {
        return OptionalLong.of(id);
      }
    }
    sender.sendMessage(
        Component.text()
            .color(NamedTextColor.RED)
            .append(
                Component.text("Argument '"),
                Component.text(str, NamedTextColor.GOLD),
                Component.text("' "),
                Component.text(message),
                Component.text(" (Must be Integer between -2,147,483,647 - 2,147,483,647)")));
    return OptionalLong.empty();
  }

  public static Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String[] dims = str.split(":");
    final String message;
    final OptionalInt width = ChatUtilities.checkIntegerValidity(dims[0]);
    final OptionalInt height = ChatUtilities.checkIntegerValidity(dims[1]);
    if (!width.isPresent()) {
      message = dims[0];
    } else if (!height.isPresent()) {
      message = dims[1];
    } else {
      return Optional.of(new int[] {width.getAsInt(), height.getAsInt()});
    }
    sender.sendMessage(
        Component.text()
            .color(NamedTextColor.RED)
            .append(Component.text("Argument '"))
            .append(Component.text(str, NamedTextColor.GOLD))
            .append(Component.text("' "))
            .append(Component.text(message))
            .append(Component.text(" is not a valid argument!"))
            .append(Component.text(" (Must be Integer)")));
    return Optional.empty();
  }

  public static OptionalLong checkLongValidity(@NotNull final String num) {
    try {
      return OptionalLong.of(Long.parseLong(num));
    } catch (final NumberFormatException e) {
      return OptionalLong.empty();
    }
  }

  public static OptionalInt checkIntegerValidity(@NotNull final String num) {
    try {
      return OptionalInt.of(Integer.parseInt(num));
    } catch (final NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  public static TextComponent getCommandUsage(@NotNull final Map<String, String> usages) {
    final TextComponent.Builder builder =
        Component.text()
            .append(Component.text("------------------", NamedTextColor.AQUA))
            .append(Component.newline());

    for (final Map.Entry<String, String> entry : usages.entrySet()) {
      builder.append(
          Component.join(
              Component.space(),
              Component.text(entry.getKey(), NamedTextColor.LIGHT_PURPLE),
              Component.text("-", NamedTextColor.GOLD),
              Component.text(entry.getValue(), NamedTextColor.AQUA),
              Component.newline()));
    }
    builder.append(Component.text("------------------", NamedTextColor.AQUA));
    return builder.build();
  }
}
