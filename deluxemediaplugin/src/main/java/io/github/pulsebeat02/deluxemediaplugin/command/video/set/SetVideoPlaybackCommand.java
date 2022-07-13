package io.github.pulsebeat02.deluxemediaplugin.command.video.set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.VideoPlayback;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetVideoPlaybackCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetVideoPlaybackCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("video-playback")
            .requires(has("deluxemediaplugin.command.video.set.videoplayback"))
            .then(
                this.argument("playback", StringArgumentType.word())
                    .suggests(this::suggestVideoPlayback)
                    .executes(this::setVideoPlayback))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestVideoPlayback(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Stream.of(VideoPlayback.values()).forEach(playback -> builder.suggest(playback.name()));
    return builder.buildFuture();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private int setVideoPlayback(final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String argument = context.getArgument("playback", String.class);
    final Optional<VideoPlayback> type = VideoPlayback.ofKey(argument);

    if (handleEmptyOptional(audience, Locale.INVALID_VIDEO_PLAYBACK.build(argument), type)) {
      return SINGLE_SUCCESS;
    }

    this.setVideoPlayback(type.get());

    audience.sendMessage(Locale.SET_VIDEO_PLAYBACK.build(argument));

    return SINGLE_SUCCESS;
  }

  private void setVideoPlayback(@NotNull final VideoPlayback type) {
    this.config.setVideoPlayback(type);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
