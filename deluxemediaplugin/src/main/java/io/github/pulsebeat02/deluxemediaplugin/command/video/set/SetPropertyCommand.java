package io.github.pulsebeat02.deluxemediaplugin.command.video.set;

import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetPropertyCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;

  public SetPropertyCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.node =
        this.literal("set")
            .requires(has("deluxemediaplugin.command.video.set"))
            .then(new SetDitheringAlgorithmCommand(plugin, config).getNode())
            .then(new SetPlayerCommand(plugin, config).getNode())
            .then(new SetAudioPlaybackCommand(plugin, config).getNode())
            .then(new SetVideoPlaybackCommand(plugin, config).getNode())
            .then(new SetDitherMapCommand(plugin, config).getNode())
            .then(new SetItemframeDimensionCommand(plugin, config).getNode())
            .then(new SetNativeDitheringCommand(plugin, config).getNode())
            .then(new SetResolutionCommand(plugin, config).getNode())
            .build();
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
