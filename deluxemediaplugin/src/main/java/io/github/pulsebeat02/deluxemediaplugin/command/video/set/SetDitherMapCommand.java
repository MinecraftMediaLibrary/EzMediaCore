package io.github.pulsebeat02.deluxemediaplugin.command.video.set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetDitherMapCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetDitherMapCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("dither-map-start")
            .requires(has("deluxemediaplugin.video.set.dither-map-start"))
            .then(
                this.argument("map", IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
                    .executes(this::setDitherMapStart))
            .build();
  }

  private int setDitherMapStart(@NotNull final CommandContext<CommandSender> context) {

    final int id = context.getArgument("map", Integer.TYPE);
    final Audience audience = this.plugin.audience().sender(context.getSource());

    this.setDitherMap(id);

    audience.sendMessage(Locale.CHANGED_VIDEO_MAP_ID.build(id));

    return SINGLE_SUCCESS;
  }

  private void setDitherMap(final int id) {
    this.config.setDitherMap(id);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
