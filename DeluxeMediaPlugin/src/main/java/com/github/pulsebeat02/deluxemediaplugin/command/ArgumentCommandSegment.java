package com.github.pulsebeat02.deluxemediaplugin.command;

import com.mojang.brigadier.tree.ArgumentCommandNode;

public interface ArgumentCommandSegment<S, T> extends CommandSegment<S, ArgumentCommandNode<S, T>> {

  /**
   * Gets the command node.
   *
   * @return command node
   */
  @Override
  ArgumentCommandNode<S, T> getCommandNode();
}
