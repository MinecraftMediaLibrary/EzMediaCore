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
package io.github.pulsebeat02.deluxemediaplugin.command.discord;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;

  public DiscordCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "discord", executor, "deluxemediaplugin.command.discord");
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(new DiscordConnectCommand(plugin).getNode())
            .then(new DiscordDisconnectCommand(plugin).getNode())
            .then(new DiscordPlayCommand(plugin).getNode())
            .then(new DiscordPauseCommand(plugin).getNode())
            .then(new DiscordResumeCommand(plugin).getNode())
            .build();
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }

  @Override
  public @NotNull Component usage() {
    return Locale.getCommandUsageComponent(
        Map.of(
            "/discord connect",
            "Connects the Discord bot to the voice channel",
            "/discord disconnect",
            "Disconnects the Discord bot from the voice channel",
            "/discord play [mrl]",
            "Plays the MRL directly through voice channel",
            "/discord pause",
            "Pauses the Discord bot player",
            "/discord resume",
            "Resumes the Discord bot player"));
  }
}
