package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.NotNull;

public interface CommandSegment<S, T extends CommandNode<S>> {

  /**
   * Gets the command node.
   *
   * @return command node.
   */
  T getCommandNode();

  /**
   * Constructs a LiteralArgumentBuilder from the given String name.
   *
   * @param name name
   * @return literal argument builder with specified name.
   */
  default LiteralArgumentBuilder<S> literal(@NotNull final String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  /**
   * Constructs a RequiredArgumentBuilder from the given String name and ArgumentType.
   *
   * @param name name
   * @param type argument type
   * @return required argument builder from name and type.
   */
  default RequiredArgumentBuilder<S, String> argument(
      @NotNull final String name, final StringArgumentType type) {
    return RequiredArgumentBuilder.argument(name, type);
  }
}
