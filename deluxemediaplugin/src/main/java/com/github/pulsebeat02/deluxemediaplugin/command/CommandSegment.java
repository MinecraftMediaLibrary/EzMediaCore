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

package com.github.pulsebeat02.deluxemediaplugin.command;

import com.mojang.brigadier.arguments.ArgumentType;
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
   * @param <T> the generic
   * @return required argument builder from name and type.
   */
  default <T> RequiredArgumentBuilder<S, T> argument(
      @NotNull final String name, final ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }
}
