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
package io.github.pulsebeat02.deluxemediaplugin.command;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Permission implements Predicate<CommandSender> {

  private final Predicate<CommandSender> delegate;

  Permission(Predicate<CommandSender> delegate) {
    while (delegate instanceof Permission) {
      delegate = ((Permission) delegate).delegate;
    }
    this.delegate = delegate;
  }

  public static Permission has(final String permission) {
    requireNonNull(permission, "permission");
    final String lowercase = permission.toLowerCase(Locale.ROOT);
    return new Permission(subject -> subject.hasPermission(lowercase));
  }

  public static Permission lacks(final String permission) {
    return has(permission).negate();
  }

  @Override
  public boolean test(final CommandSender subject) {
    return this.delegate.test(subject);
  }

  @Override
  public @NotNull Permission and(@NotNull final Predicate<? super CommandSender> other) {
    return new Permission(this.delegate.and(requireNonNull(other, "other")));
  }

  public @NotNull Permission and(@NotNull final String other) {
    return this.and(has(other));
  }

  @Override
  public @NotNull Permission or(@NotNull final Predicate<? super CommandSender> other) {
    return new Permission(this.delegate.or(requireNonNull(other, "other")));
  }

  public @NotNull Permission or(@NotNull final String other) {
    return new Permission(this.or(has(other)));
  }

  @Override
  public @NotNull Permission negate() {
    return new Permission(this.delegate.negate());
  }
}
