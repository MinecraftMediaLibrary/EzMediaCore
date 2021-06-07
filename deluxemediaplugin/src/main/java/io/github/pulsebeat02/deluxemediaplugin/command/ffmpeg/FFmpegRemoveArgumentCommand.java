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

package io.github.pulsebeat02.deluxemediaplugin.command.ffmpeg;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegCustomCommandExecutor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public final class FFmpegRemoveArgumentCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;
  private final FFmpegCustomCommandExecutor ffmpeg;

  public FFmpegRemoveArgumentCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final FFmpegCustomCommandExecutor executor) {
    this.plugin = plugin;
    ffmpeg = executor;
    node =
        literal("remove")
            .then(
                argument("argument", StringArgumentType.greedyString())
                    .executes(this::removeArgument))
            .then(
                argument("index", IntegerArgumentType.integer())
                    .executes(this::removeIndexArgument))
            .build();
  }

  private int removeIndexArgument(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final int index = context.getArgument("index", int.class);
    final String arg = ffmpeg.getArguments().get(index);
    ffmpeg.removeArgument(index);
    audience.sendMessage(
        format(
            ofChildren(
                text("Removed arguments ", GOLD),
                text(arg, AQUA),
                text(" from the FFmpeg command at index ", GOLD),
                text(index, AQUA))));
    return SINGLE_SUCCESS;
  }

  private int removeArgument(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final String argument = context.getArgument("argument", String.class);
    ffmpeg.removeArgument(argument);
    audience.sendMessage(
        format(
            ofChildren(
                text("Removed arguments ", GOLD),
                text(argument, AQUA),
                text(" from the FFmpeg command.", GOLD))));
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
