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

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.gold;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.audio.MusicManager;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
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
            .then(this.literal("connect").executes(this::connect))
            .then(this.literal("disconnect").executes(this::disconnect))
            .then(
                this.literal("play")
                    .then(
                        this.argument("mrl", StringArgumentType.greedyString())
                            .executes(this::play)))
            .then(this.literal("pause").executes(this::pause))
            .then(this.literal("resume").executes(this::resume))
            .build();
  }

  private int disconnect(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.audience().sender(context.getSource());
    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }
    this.plugin().getMediaBot().getMusicManager().leaveVoiceChannel();
    gold(audience, "Successfully disconnected from voice channel!");
    return SINGLE_SUCCESS;
  }

  private int connect(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.audience().sender(context.getSource());
    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }
    this.plugin().getMediaBot().getMusicManager().joinVoiceChannel();
    gold(audience, "Successfully connected to voice channel!");
    return SINGLE_SUCCESS;
  }

  private int play(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }
    final MusicManager manager = this.plugin().getMediaBot().getMusicManager();
    manager.joinVoiceChannel();
    manager.addTrack(mrl);
    gold(audience, "Successfully started audio on MRL %s!".formatted(mrl));
    return SINGLE_SUCCESS;
  }

  private int pause(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.audience().sender(context.getSource());
    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }
    this.plugin().getMediaBot().getMusicManager().puaseTrack();
    gold(audience, "Successfully paused track!");
    return SINGLE_SUCCESS;
  }

  private int resume(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.audience().sender(context.getSource());
    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }
    this.plugin().getMediaBot().getMusicManager().resumeTrack();
    gold(audience, "Successfully resumed track!");
    return SINGLE_SUCCESS;
  }

  private boolean checkDiscordStatus(@NotNull final Audience audience) {
    if (this.plugin().getMediaBot() == null) {
      red(audience, "Discord bot not setup yet or invalid settings in bot.yml!");
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }

  @Override
  public @NotNull Component usage() {
    return ChatUtils.getCommandUsage(
        Map.of(
            "/discord connect",
            "Connects to the voice channel",
            "/discord disconnect",
            "Disconnects from the voice channel",
            "/discord play [mrl]",
            "Plays the MRL directly through voice channel",
            "/discord pause",
            "Pauses the audio player",
            "/discord resume",
            "Resumes the audio player"));
  }
}
