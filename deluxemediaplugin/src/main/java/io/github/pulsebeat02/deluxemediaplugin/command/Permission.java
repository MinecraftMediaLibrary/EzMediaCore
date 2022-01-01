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
  public @NotNull Permission and(final @NotNull Predicate<? super CommandSender> other) {
    return new Permission(this.delegate.and(requireNonNull(other, "other")));
  }

  public @NotNull Permission and(final @NotNull String other) {
    return this.and(has(other));
  }

  @Override
  public @NotNull Permission or(final @NotNull Predicate<? super CommandSender> other) {
    return new Permission(this.delegate.or(requireNonNull(other, "other")));
  }

  public @NotNull Permission or(final @NotNull String other) {
    return new Permission(this.or(has(other)));
  }

  @Override
  public @NotNull Permission negate() {
    return new Permission(this.delegate.negate());
  }
}
