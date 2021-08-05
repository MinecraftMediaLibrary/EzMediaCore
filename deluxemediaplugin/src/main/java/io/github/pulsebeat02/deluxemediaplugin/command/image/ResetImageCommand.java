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

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.ezmediacore.image.Image;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public final class ResetImageCommand implements CommandSegment.Literal<CommandSender>, Listener {

  private final LiteralCommandNode<CommandSender> node;
  private final ImageCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public ResetImageCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ImageCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("purge")
            .then(
                this.literal("map")
                    .then(
                        this.argument(
                                "id", IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
                            .executes(this::purgeMap)))
            .then(this.literal("all").executes(this::purgeAllMaps))
            .build();
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  private int purgeMap(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final int id = context.getArgument("id", int.class);

    final Optional<Image> image =
        this.plugin.getPictureManager().getImages().stream()
            .filter(img -> img.getMaps().contains(id))
            .findAny();
    if (!image.isPresent()) {
      red(audience, "The image you request purge from the map is not loaded!");
      return SINGLE_SUCCESS;
    }

    image.get().resetMaps();

    audience.sendMessage(
        format(ofChildren(text("Successfully purged all maps with id ", GOLD), text(id, AQUA))));

    return SINGLE_SUCCESS;
  }

  private int purgeAllMaps(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final Audience audience = this.plugin.audience().sender(sender);
    if (!(sender instanceof Player)) {
      red(audience, "You must be a player to execute this command!");
      return SINGLE_SUCCESS;
    }

    this.attributes.getListen().add(((Player) sender).getUniqueId());

    red(
        audience,
        "Are you sure you want to purge all maps? Type YES (all caps) if you would like to continue...");

    return SINGLE_SUCCESS;
  }

  @EventHandler
  public void onPlayerChat(final AsyncPlayerChatEvent event) {

    final Player p = event.getPlayer();
    final UUID uuid = p.getUniqueId();
    final Set<UUID> listen = this.attributes.getListen();

    if (listen.contains(uuid)) {

      event.setCancelled(true);
      final Audience audience = this.plugin.audience().player(p);

      if (event.getMessage().equals("YES")) {
        final List<Image> images = this.plugin.getPictureManager().getImages();
        images.forEach(Image::resetMaps);
        images.clear();
        red(audience, "Successfully purged all images!");
      } else {
        red(audience, "Cancelled purge of all images!");
      }
      listen.remove(uuid);
    }
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
