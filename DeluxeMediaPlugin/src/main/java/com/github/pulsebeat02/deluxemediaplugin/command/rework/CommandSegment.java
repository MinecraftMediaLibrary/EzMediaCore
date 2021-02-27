package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.NotNull;

public interface CommandSegment<S, T extends CommandNode<S>> {

    T getCommandNode();

    default LiteralArgumentBuilder<S> literal(@NotNull final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    default <K> RequiredArgumentBuilder<S, T> argument(@NotNull final String name, @NotNull final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

}
