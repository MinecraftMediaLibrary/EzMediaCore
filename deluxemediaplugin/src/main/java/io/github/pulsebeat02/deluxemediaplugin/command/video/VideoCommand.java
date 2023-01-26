/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.deluxemediaplugin.command.video;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.video.load.LoadVideoCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.video.set.SetPropertyCommand;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.locale.LocaleParent;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public final class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    final ScreenConfig config = plugin.getScreenConfig();
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(new LoadVideoCommand(plugin, config).getNode())
            .then(new VideoDumpThreadsCommand(plugin).getNode())
            .then(new VideoPauseCommand(plugin, config).getNode())
            .then(new VideoDestroyCommand(plugin, config).getNode())
            .then(new VideoCancelProcessingCommand(plugin, config).getNode())
            .then(new VideoPlayCommand(plugin, config).getNode())
            .then(new VideoResourcepackCommand(plugin, config).getNode())
            .then(new SetPropertyCommand(plugin, config).getNode())
            .build();
  }

  @Override
  public Component usage() {
    return LocaleParent.getCommandUsageComponent(
        Map.of(
            "/video load [input] [argument]",
            "Loads the video",
            "/video dump-threads",
            "Dumps threads for video player (debugging purposes)",
            "/video play [target selector]",
            "Plays the video player for the selected entities",
            "/video pause",
            "Pauses the video player",
            "/video destroy",
            "Destroys the video player",
            "/video cancel-processing",
            "Cancels video processing",
            "/video load [target selector]",
            "Loads the video resourcepack for the target selected entities",
            "/video set [property] [value]",
            "Sets the video player property to the new value"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
