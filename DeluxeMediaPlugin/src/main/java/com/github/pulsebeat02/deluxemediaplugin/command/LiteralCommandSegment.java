package com.github.pulsebeat02.deluxemediaplugin.command;

import com.mojang.brigadier.tree.LiteralCommandNode;

public interface LiteralCommandSegment<S> extends CommandSegment<S, LiteralCommandNode<S>> {

  /**
   * Gets the command node.
   *
   * @return literal command node.
   */
  @Override
  LiteralCommandNode<S> getCommandNode();
}
