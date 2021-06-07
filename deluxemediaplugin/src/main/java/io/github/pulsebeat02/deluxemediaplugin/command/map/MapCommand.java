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

package io.github.pulsebeat02.deluxemediaplugin.command.map;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.MapUtilities;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class MapCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;

  public MapCommand(@NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "map", executor, "deluxemediaplugin.command.map", "");
    node =
        literal(getName())
            .requires(super::testPermission)
            .then(argument("id", IntegerArgumentType.integer()).executes(this::giveMap))
            .build();
  }

  private int giveMap(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Audience audience = getPlugin().audience().sender(sender);
    if (!(sender instanceof Player)) {
      audience.sendMessage(text("You must be a Player to run this command!", RED));
      return SINGLE_SUCCESS;
    }
    final int id = context.getArgument("id", int.class);
    ((Player) sender).getInventory().addItem(MapUtilities.getMapFromID(id));
    audience.sendMessage(format(ofChildren(text("Gave map with id ", GOLD), text(id, AQUA))));
    return SINGLE_SUCCESS;
  }

  @Override
  public Component usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.of("/map [id]", "Gives a map to the player with the specific id"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getCommandNode() {
    return node;
  }
}
