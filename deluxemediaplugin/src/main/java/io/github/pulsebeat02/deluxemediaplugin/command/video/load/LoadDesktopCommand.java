package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.ezmediacore.player.buffered.JCodecMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class LoadDesktopCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;
  private final LoadVideoCommand command;

  public LoadDesktopCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig config,
      @NotNull final LoadVideoCommand command) {
    this.plugin = plugin;
    this.config = config;
    this.command = command;
    this.node =
        this.literal("desktop")
            .requires(has("deluxemediaplugin.command.video.load.desktop"))
            .executes(this::handleDesktop)
            .build();
  }

  private int handleDesktop(final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.checkInvalidPlayer(audience)) {
      return SINGLE_SUCCESS;
    }

    this.setDesktopMedia();

    audience.sendMessage(Locale.SET_MEDIA.build("Desktop capture"));

    this.command.loadVideo(audience);

    return SINGLE_SUCCESS;
  }

  private void setDesktopMedia() {
    this.config.setMedia(DesktopInput.defaultDesktop());
  }

  private boolean checkInvalidPlayer(@NotNull final Audience audience) {
    return handleTrue(
        audience,
        Locale.ERR_INVALID_PLAYER_MEDIA.build("JCodec", "Desktop capture"),
        this.config.getPlayer() instanceof JCodecMediaPlayer);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
