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

package io.github.pulsebeat02.deluxemediaplugin.command.ffmpeg;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public final class FFmpegCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;
  private final FFmpegCommandExecutor ffmpeg;

  public FFmpegCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "ffmpeg", executor, "deluxemediaplugin.command.ffmpeg");
    this.ffmpeg = FFmpegCommandExecutor.ofFFmpegExecutor(plugin.library());
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(new FFmpegResetCommand(plugin, this.ffmpeg).getNode())
            .then(new FFmpegAddArgumentCommand(plugin, this.ffmpeg).getNode())
            .then(new FFmpegRemoveArgumentCommand(plugin, this.ffmpeg).getNode())
            .then(new FFmpegListArgumentsCommand(plugin, this.ffmpeg).getNode())
            .then(new FFmpegRunCommand(plugin, this.ffmpeg).getNode())
            .build();
  }

  @Override
  public @NotNull Component usage() {
    return Locale.getCommandUsageComponent(
        Map.of(
            "/ffmpeg reset",
            "Reset all arguments in the FFmpeg command",
            "/ffmpeg add [key=value] [key=value] ...",
            "Adds the arguments to the end of the FFmpeg command",
            "/ffmpeg add [index] [key=value] [key=value] ...",
            "Adds the arguments to the specified index of the FFmpeg command",
            "/ffmpeg remove [key=value]",
            "Removes the specified argument from the FFmpeg command",
            "/ffmpeg remove [index]",
            "Removes the specified argument at the index",
            "/ffmpeg list-arguments",
            "Lists all arguments of the FFmpeg command",
            "/ffmpeg run",
            "Runes the FFmpeg command"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
