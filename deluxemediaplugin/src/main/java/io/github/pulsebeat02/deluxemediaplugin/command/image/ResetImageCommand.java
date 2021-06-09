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

package io.github.pulsebeat02.deluxemediaplugin.command.image;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImage;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImageProxy;
import io.github.pulsebeat02.minecraftmedialibrary.image.gif.DynamicImage;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class ResetImageCommand implements CommandSegment.Literal<CommandSender>, Listener {

  private final LiteralCommandNode<CommandSender> node;
  private final ImageCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public ResetImageCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ImageCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    node =
        literal("purge")
            .then(
                literal("map")
                    .then(
                        argument("id", IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
                            .executes(this::purgeMap)))
            .then(literal("all").executes(this::purgeAllMaps))
            .build();
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  private int purgeMap(@NotNull final CommandContext<CommandSender> context) {
    final MediaLibrary library = plugin.getLibrary();
    final int id = context.getArgument("id", int.class);
    attributes.getImages().removeIf(x -> x.getMap() == id);
    StaticImage.resetMap(library, id);
    DynamicImage.resetMap(library, id);
    plugin
        .audience()
        .sender(context.getSource())
        .sendMessage(
            format(
                ofChildren(text("Successfully purged all maps with id ", GOLD), text(id, AQUA))));
    return SINGLE_SUCCESS;
  }

  private int purgeAllMaps(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Audience audience = plugin.audience().sender(sender);
    if (!(sender instanceof Player)) {
      audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
      return SINGLE_SUCCESS;
    }
    attributes.getListen().add(((Player) sender).getUniqueId());
    audience.sendMessage(
        format(
            text(
                "Are you sure you want to purge all maps? Type YES (in full caps) if you would like to continue...",
                RED)));
    return SINGLE_SUCCESS;
  }

  @EventHandler
  public void onPlayerChat(final AsyncPlayerChatEvent event) {
    final Player p = event.getPlayer();
    final UUID uuid = p.getUniqueId();
    final Set<UUID> listen = attributes.getListen();
    if (listen.contains(uuid)) {
      event.setCancelled(true);
      final Audience audience = plugin.audience().player(p);
      if (event.getMessage().equals("YES")) {
        final MediaLibrary library = plugin.getLibrary();
        final Set<StaticImageProxy> images = attributes.getImages();
        images.forEach(x -> StaticImage.resetMap(library, x.getMap()));
        images.forEach(x -> DynamicImage.resetMap(library, x.getMap()));
        images.clear();
        audience.sendMessage(format(text("Successfully purged all image maps!", RED)));
      } else {
        audience.sendMessage(format(text("Cancelled purge of all image maps!", RED)));
      }
      listen.remove(uuid);
    }
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
