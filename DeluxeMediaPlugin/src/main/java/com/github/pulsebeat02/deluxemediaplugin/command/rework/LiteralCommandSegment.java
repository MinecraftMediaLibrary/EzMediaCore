package com.github.pulsebeat02.deluxemediaplugin.command.rework;

public interface LiteralCommandSegment<S> extends CommandSegment<S,T> {

    @Override
    T getCommandNode();
}

