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
