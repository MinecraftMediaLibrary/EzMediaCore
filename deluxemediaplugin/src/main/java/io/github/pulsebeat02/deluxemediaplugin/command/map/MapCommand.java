/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.deluxemediaplugin.command.map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import io.github.pulsebeat02.ezmediacore.utility.MapUtils;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class MapCommand extends BaseCommand {

	private final LiteralCommandNode<CommandSender> node;

	public MapCommand(@NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
		super(plugin, "map", executor, "deluxemediaplugin.command.map", "");
		this.node =
				this.literal(this.getName())
						.requires(super::testPermission)
						.then(
								this.argument("id", IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
										.executes(this::giveMap))
						.then(
								this.argument("ids", StringArgumentType.greedyString())
										.executes(this::giveMultipleMaps))
						.build();
	}

	private int giveMap(@NotNull final CommandContext<CommandSender> context) {

		final CommandSender sender = context.getSource();
		final Audience audience = this.plugin().audience().sender(sender);
		final int id = context.getArgument("id", int.class);

		if (!(sender instanceof Player)) {
			red(audience, "You must be a player to execute this command!");
			return SINGLE_SUCCESS;
		}

		((Player) sender).getInventory().addItem(MapUtils.getMapFromID(id));

		audience.sendMessage(format(join(
				noSeparators(), text("Gave map with id ", GOLD), text(id, AQUA))));

		return SINGLE_SUCCESS;
	}

	private int giveMultipleMaps(@NotNull final CommandContext<CommandSender> context) {

		final CommandSender sender = context.getSource();
		final Audience audience = this.plugin().audience().sender(sender);
		final String[] bits = context.getArgument("ids", String.class).split("-");

		final int start;
		final int end;
		try {
			start = Integer.parseInt(bits[0]);
			end = Integer.parseInt(bits[1]);
		} catch (final NumberFormatException e) {
			red(audience, "Invalid format! Must follow [starting-id]-[ending-id]");
			return SINGLE_SUCCESS;
		}

		if (!(sender instanceof final Player player)) {
			red(audience, "You must be a player to execute this command!");
			return SINGLE_SUCCESS;
		}

		final PlayerInventory inventory = player.getInventory();
		boolean noSpace = false;
		for (int id = start; id <= end; id++) {
			final ItemStack stack = MapUtils.getMapFromID(id);
			if (inventory.firstEmpty() == -1) {
				noSpace = true;
			}
			if (noSpace) {
				player.getWorld().dropItem(player.getLocation(), stack);
			} else {
				inventory.addItem(stack);
			}
		}

		audience.sendMessage(
				format(
						join(
								noSeparators(),
								text("Gave maps between IDs ", GOLD),
								text(start, AQUA),
								text(" and ", GOLD),
								text(end, AQUA))));

		return SINGLE_SUCCESS;
	}

	@Override
	public @NotNull Component usage() {
		return ChatUtils.getCommandUsage(
				Map.of(
						"/map [id]",
						"Gives a map to the player with the specific id",
						"/map [starting-id]-[ending-id]",
						"Gives all maps between starting-id to ending-id (inclusive)"));
	}

	@Override
	public @NotNull LiteralCommandNode<CommandSender> node() {
		return this.node;
	}
}
