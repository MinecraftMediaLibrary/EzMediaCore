package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNonNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.VideoPlayback;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VideoPlayCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public VideoPlayCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("play")
            .requires(has("deluxemediaplugin.command.video.play"))
            .then(
                this.argument("entities", StringArgumentType.greedyString())
                    .executes(this::playVideo))
            .build();
  }

  private int playVideo(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final Audience audience = this.plugin.audience().sender(sender);
    final String selectors = context.getArgument("entities", String.class);

    if (this.attemptSelection(sender, audience, selectors)) {
      return SINGLE_SUCCESS;
    }

    final List<Entity> entities =
        this.plugin.getBootstrap().getServer().selectEntities(sender, selectors);
    if (this.checkSelectors(audience, entities)) {
      return SINGLE_SUCCESS;
    }

    if (this.config.mediaNotSpecified(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.config.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.isValidScoreboardDimension(audience)) {
      return SINGLE_SUCCESS;
    }

    this.releaseIfPlaying();
    this.createVideoPlayer(sender, entities);
    this.setProperAudioHandler();
    this.cancelStream();
    this.handleStreamPlayers(audience);
    this.startPlayer();
    this.sendPlayInformation(audience);

    return SINGLE_SUCCESS;
  }

  private boolean attemptSelection(
          @NotNull final CommandSender sender,
          @NotNull final Audience audience,
          @NotNull final String selector) {
    final boolean status;
    try {
      this.plugin.getBootstrap().getServer().selectEntities(sender, selector);
      return false;
    } catch (final IllegalArgumentException e) {
      status = true;
    }
    return handleTrue(audience, Locale.ERR_INVALID_TARGET_SELECTOR.build(), status);
  }

  private boolean isValidScoreboardDimension(@NotNull final Audience audience) {
    final int width = this.config.getResolutionWidth();
    final int height = this.config.getResolutionHeight();
    final boolean valid =
        this.config.getVideoPlayback() != VideoPlayback.SCOREBOARD
            || width >= 0 && width <= 32 && height >= 0 && height <= 16;
    return handleFalse(audience, Locale.ERR_SCOREBOARD_DIMENSION.build(), valid);
  }

  private void startPlayer() {
    this.config.getPlayer().start(this.config.getMedia());
  }

  private void handleStreamPlayers(@NotNull final Audience audience) {
    this.config
        .getAudioPlayback()
        .getHandle()
        .setAudioHandler(this.plugin, this.config, audience, this.config.getMedia().getInput());
  }

  private void cancelStream() {
    final EnhancedExecution stream = this.config.getStream();
    Nill.ifNot(stream, () -> Try.closeable(stream));
  }

  private void setProperAudioHandler() {
    this.config.getAudioPlayback().getHandle().setProperAudioHandler(this.plugin, this.config);
  }

  private void sendPlayInformation(@NotNull final Audience audience) {
    final Input mrl = this.config.getMedia();
    handleNonNull(audience, Locale.STARTING_VIDEO.build(mrl.getInput()), mrl);
  }

  private void createVideoPlayer(
      @NotNull final CommandSender sender, @NotNull final List<Entity> entities) {
    final VideoPlayback type = this.config.getVideoPlayback();
    type.getHandle().createVideoPlayer(this.plugin, this.config, sender, this.convert(entities));
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

  private boolean checkSelectors(
      @NotNull final Audience audience, @NotNull final List<Entity> entities) {
    final boolean valid =
        entities.stream().anyMatch(Predicate.not(entity -> entity instanceof Player));
    return handleTrue(audience, Locale.ERR_INVALID_TARGET_SELECTOR.build(), valid);
  }

  private List<? extends Player> convert(@NotNull final List<Entity> entities) {
    return entities.stream().map(entity -> (Player) entity).collect(Collectors.toList());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
