/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.deluxemediaplugin.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public interface LocaleParent {

  TranslationManager MANAGER = new TranslationManager();

  NullComponent<Sender> PREFIX =
      () ->
          text()
              .color(AQUA)
              .append(
                  text('['), text("DeluxeMediaPlugin", GOLD), text(']'), space(), text("»", GRAY))
              .build();

  static @NotNull NullComponent<Sender> error(@NotNull final String key) {
    return () -> MANAGER.render(translatable(key, RED));
  }

  static @NotNull <T> UniComponent<Sender, T> error(
      @NotNull final String key, @Nullable final Function<T, String> function) {
    return argument -> error0(key, List.of(text(createFinalText(argument, function), AQUA)));
  }

  static @NotNull <T, U> BiComponent<Sender, T, U> error(
      @NotNull final String key,
      @Nullable final Function<T, String> function1,
      @Nullable final Function<U, String> function2) {
    return (argument1, argument2) ->
        error0(
            key,
            List.of(
                text(createFinalText(argument1, function1), AQUA),
                text(createFinalText(argument2, function2), AQUA)));
  }

  static @NotNull NullComponent<Sender> info(@NotNull final String key) {
    return () -> format(translatable(key, GOLD));
  }

  static @NotNull <T> UniComponent<Sender, T> info(
      @NotNull final String key, @Nullable final Function<T, String> function) {
    return argument -> format(info0(key, List.of(text(createFinalText(argument, function), AQUA))));
  }

  static @NotNull <T, U> BiComponent<Sender, T, U> info(
      @NotNull final String key,
      @Nullable final Function<T, String> function1,
      @Nullable final Function<U, String> function2) {
    return (argument1, argument2) ->
        format(info0(
            key,
            List.of(
                text(createFinalText(argument1, function1), AQUA),
                text(createFinalText(argument2, function2), AQUA))));
  }

  static @NotNull Component info0(
      @NotNull final String key, @NotNull final List<Component> arguments) {
    return internal0(key, GOLD, arguments);
  }

  static @NotNull Component error0(
      @NotNull final String key, @NotNull final List<Component> arguments) {
    return internal0(key, RED, arguments);
  }

  static @NotNull Component internal0(
      @NotNull final String key,
      @NotNull final NamedTextColor color,
      @NotNull final List<Component> arguments) {
    return MANAGER.render(translatable(key, color, arguments));
  }

  static @NotNull <T> String createFinalText(
      @NotNull final T argument, @Nullable final Function<T, String> function) {
    return function == null ? argument.toString() : function.apply(argument);
  }

  static @NotNull Component format(@NotNull final Component message) {
    return MANAGER.render(join(separator(space()), PREFIX.build(), message));
  }

  static @NotNull TextComponent getCommandUsageComponent(
      @NotNull final Map<String, String> usages) {
    final TextComponent.Builder builder = text();
    usages.forEach((key, value) -> builder.append(createUsageComponent(key, value)));
    return builder.build();
  }

  static @NotNull Component createUsageComponent(
      @NotNull final String key, @NotNull final String value) {
    final ComponentLike[] components = {
      text(key, LIGHT_PURPLE), text("-", GOLD), text(value, AQUA), newline()
    };
    return join(separator(space()), components);
  }

  static @NotNull Component getComponent(
      @NotNull final String largeString, @NotNull final Function<String, Component> function) {
    final TextComponent.Builder component = text();
    final String[] split = largeString.split(System.lineSeparator());
    for (final String line : split) {
      component.append(function.apply(line));
    }
    return component.build();
  }

  @FunctionalInterface
  interface NullComponent<S extends Sender> {

    Component build();

    default void send(@NotNull final S sender) {
      sender.sendMessage(format(this.build()));
    }
  }

  @FunctionalInterface
  interface UniComponent<S extends Sender, A0> {

    Component build(A0 arg0);

    default void send(@NotNull final S sender, final A0 arg0) {
      sender.sendMessage(format(this.build(arg0)));
    }
  }

  @FunctionalInterface
  interface BiComponent<S extends Sender, A0, A1> {

    Component build(A0 arg0, A1 arg1);

    default void send(@NotNull final S sender, @NotNull final A0 arg0, @NotNull final A1 arg1) {
      sender.sendMessage(format(this.build(arg0, arg1)));
    }
  }

  @FunctionalInterface
  interface TriComponent<S extends Sender, A0, A1, A2> {

    Component build(A0 arg0, A1 arg1, A2 arg2);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2)));
    }
  }

  @FunctionalInterface
  interface QuadComponent<S extends Sender, A0, A1, A2, A3> {

    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3)));
    }
  }

  @FunctionalInterface
  interface PentaComponent<S extends Sender, A0, A1, A2, A3, A4> {

    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3,
        @NotNull final A4 arg4) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3, arg4)));
    }
  }

  @FunctionalInterface
  interface HexaComponent<S extends Sender, A0, A1, A2, A3, A4, A5> {

    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3,
        @NotNull final A4 arg4,
        @NotNull final A5 arg5) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3, arg4, arg5)));
    }
  }
}
