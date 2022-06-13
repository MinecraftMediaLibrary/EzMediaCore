package io.github.pulsebeat02.deluxemediaplugin.command.discord;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static java.util.Objects.requireNonNull;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.audio.MusicManager;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class DiscordPlayCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;

  public DiscordPlayCommand(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    this.node =
        this.literal("play")
            .requires(has("deluxemediaplugin.command.discord.play"))
            .then(
                this.argument("mrl", StringArgumentType.greedyString())
                    .executes(this::playDiscordBot))
            .build();
  }

  private int playDiscordBot(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);

    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }

    this.playMedia(mrl);

    audience.sendMessage(Locale.START_TRACK_DISCORD.build(mrl));

    return SINGLE_SUCCESS;
  }

  private boolean checkDiscordStatus(@NotNull final Audience audience) {
    return handleNull(audience, Locale.ERR_INVALID_DISCORD_BOT.build(), this.plugin.getMediaBot());
  }

  private void playMedia(@NotNull final String mrl) {
    final MusicManager manager = requireNonNull(this.plugin.getMediaBot()).getMusicManager();
    manager.joinVoiceChannel();
    manager.addTrack(mrl);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
