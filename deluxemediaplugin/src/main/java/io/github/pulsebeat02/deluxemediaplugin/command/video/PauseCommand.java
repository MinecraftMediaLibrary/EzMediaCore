package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class PauseCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public PauseCommand(@NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("pause")
            .requires(has("deluxemediaplugin.video.pause"))
            .executes(this::pauseVideo)
            .build();
  }

  private int pauseVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.config.mediaNotSpecified(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.config.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.config.mediaUninitialized(audience)) {
      return SINGLE_SUCCESS;
    }

    this.pauseDiscordMusic();
    this.pauseStreamMusic();
    this.pauseVideoPlayer();

    audience.sendMessage(Locale.PAUSE_VIDEO.build());

    return SINGLE_SUCCESS;
  }

  private void pauseVideoPlayer() {
    final VideoPlayer player = this.config.getPlayer();
    Nill.ifNot(player, player::pause);
  }

  private void pauseStreamMusic() {
    final EnhancedExecution stream = this.config.getStream();
    Nill.ifNot(stream, () -> Try.closeable(stream));
  }

  private void pauseDiscordMusic() {
    final MediaBot bot = this.plugin.getMediaBot();
    Nill.ifNot(bot, () -> bot.getMusicManager().pauseTrack());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
