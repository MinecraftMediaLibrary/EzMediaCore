package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class VideoDestroyCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public VideoDestroyCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("destroy")
            .requires(has("deluxemediaplugin.command.video.destroy"))
            .executes(this::destroyVideo)
            .build();
  }

  private int destroyVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.config.mediaNotSpecified(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.config.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    this.releaseIfPlaying();

    audience.sendMessage(Locale.RELEASE_VIDEO.build());

    return SINGLE_SUCCESS;
  }

  private void releaseIfPlaying() {

    final VideoPlayer player = this.config.getPlayer();
    Nill.ifNot(player, () -> this.releasePlayer(player));

    this.config.setPlayer(null);
  }

  private void releasePlayer(@NotNull final VideoPlayer player) {
    final PlayerControls state = player.getPlayerState();
    if (state != PlayerControls.RELEASE) {
      if (state == PlayerControls.START || state == PlayerControls.RESUME) {
        player.pause();
      }
      player.release();
    }
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
