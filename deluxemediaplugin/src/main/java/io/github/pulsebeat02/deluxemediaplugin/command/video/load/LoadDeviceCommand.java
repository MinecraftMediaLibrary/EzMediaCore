package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class LoadDeviceCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;
  private final LoadVideoCommand command;

  public LoadDeviceCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig config,
      @NotNull final LoadVideoCommand command) {
    this.plugin = plugin;
    this.config = config;
    this.command = command;
    this.node =
        this.literal("device")
            .requires(has("deluxemediaplugin.command.video.load.device"))
            .then(
                this.argument("name", StringArgumentType.greedyString())
                    .executes(this::handleDevice))
            .build();
  }

  private int handleDevice(final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String media = context.getArgument("name", String.class);

    if (this.checkInvalidPlayer(audience)) {
      return SINGLE_SUCCESS;
    }

    this.setDeviceMedia(media);

    audience.sendMessage(Locale.SET_MEDIA.build("Device capture"));

    this.command.loadVideo(audience);

    return SINGLE_SUCCESS;
  }

  private void setDeviceMedia(@NotNull final String media) {
    this.config.setMedia(DeviceInput.ofDeviceName(media));
  }

  private boolean checkInvalidPlayer(@NotNull final Audience audience) {
    return handleFalse(
        audience,
        Locale.ERR_INVALID_PLAYER_MEDIA.build("JCodec and VLC Media Player", "Device capture"),
        this.config.getPlayer() instanceof FFmpegMediaPlayer);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
