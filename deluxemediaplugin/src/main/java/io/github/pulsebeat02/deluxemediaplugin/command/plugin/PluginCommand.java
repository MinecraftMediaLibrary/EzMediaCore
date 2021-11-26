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

package io.github.pulsebeat02.deluxemediaplugin.command.plugin;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.component.ChatUtils;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class PluginCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;

  public PluginCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(
        plugin,
        "deluxemediaplugin",
        executor,
        "deluxemediaplugin.command.deluxemediaplugin",
        "dmp");
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(this.literal("info").executes(this::sendInformation))
            .build();
  }

  private int sendInformation(@NotNull final CommandContext<CommandSender> context) {
    this.plugin().audience().sender(context.getSource()).sendMessage(Locale.PLUGIN_AUTHORS.build());
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull Component usage() {
    return ChatUtils.getCommandUsage(
        Map.of("/deluxemediaplugin info", "Displays information about the plugin"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
