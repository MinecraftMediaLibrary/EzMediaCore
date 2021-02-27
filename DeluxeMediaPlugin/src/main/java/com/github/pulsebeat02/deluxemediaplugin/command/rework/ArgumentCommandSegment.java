package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.mojang.brigadier.tree.ArgumentCommandNode;

public interface ArgumentCommandSegment<S, T> extends CommandSegment<S, ArgumentCommandNode<S, T>> {

    @Override
    ArgumentCommandNode<S, T> getCommandNode();
}

