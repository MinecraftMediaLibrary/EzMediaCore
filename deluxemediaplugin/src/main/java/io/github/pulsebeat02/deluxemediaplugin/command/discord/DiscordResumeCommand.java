package io.github.pulsebeat02.deluxemediaplugin.command.discord;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static java.util.Objects.requireNonNull;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class DiscordResumeCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;

  public DiscordResumeCommand(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    this.node =
        this.literal("resume")
            .requires(has("deluxemediaplugin.command.discord.resume"))
            .executes(this::resumeDiscordBot)
            .build();
  }

  private int resumeDiscordBot(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.checkDiscordStatus(audience)) {
      return SINGLE_SUCCESS;
    }

    this.resume();

    audience.sendMessage(Locale.RESUMED_TRACK_DISCORD.build());

    return SINGLE_SUCCESS;
  }

  private void resume() {
    requireNonNull(this.plugin.getMediaBot()).getMusicManager().resumeTrack();
  }

  private boolean checkDiscordStatus(@NotNull final Audience audience) {
    return handleNull(audience, Locale.ERR_INVALID_DISCORD_BOT.build(), this.plugin.getMediaBot());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
