package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg.AudioPlayerStreamSendHandler;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
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
    this.releaseNativeProcesses();

    audience.sendMessage(Locale.DESTROY_VIDEO.build());

    return SINGLE_SUCCESS;
  }

  private void releaseIfPlaying() {

    final VideoPlayer player = this.config.getPlayer();
    Nill.ifNot(player, () -> this.releasePlayer(player));

    this.config.setPlayer(null);
  }

  private void releaseNativeProcesses() {
    final EnhancedExecution extractor = this.config.getExtractor();
    final AudioPlayerStreamSendHandler handler = this.config.getDiscordHandler();
    final EnhancedExecution stream = this.config.getStream();
    Nill.ifNot(extractor, () -> Try.closeable(extractor));
    Nill.ifNot(handler, () -> handler.pause());
    Nill.ifNot(stream, () -> Try.closeable(stream));
  }

  private void releasePlayer(@NotNull final VideoPlayer player) {
    final PlayerControls state = player.getPlayerState();
    if (state != PlayerControls.RELEASE) {
      player.pause();
      player.release();
    }
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
