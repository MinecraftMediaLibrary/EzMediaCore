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

package io.github.pulsebeat02.deluxemediaplugin.command.dither;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class DitherCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;

  public DitherCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "dither", executor, "deluxemediaplugin.command.dither", "");
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder.requires(super::testPermission).then(literal("list").executes(this::listSettings));
    literalNode = builder.build();
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }

  @Override
  public Component usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.of("/dither list", "Lists all possible dithering algorithms to choose"));
  }

  private int listSettings(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    audience.sendMessage(
        ChatUtilities.formatMessage(Component.text("Possible Settings: ", NamedTextColor.GOLD)));

    // List all possible dithering settings the user can select
    for (final DitherSetting setting : DitherSetting.values()) {
      audience.sendMessage(Component.text(setting.name(), NamedTextColor.AQUA));
    }
    return 1;
  }
}
