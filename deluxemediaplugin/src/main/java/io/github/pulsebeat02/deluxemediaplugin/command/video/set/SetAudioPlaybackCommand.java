package io.github.pulsebeat02.deluxemediaplugin.command.video.set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioPlayback;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetAudioPlaybackCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetAudioPlaybackCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("audio-playback")
            .requires(has("deluxemediaplugin.video.set.audioplayback"))
            .then(
                this.argument("playback", StringArgumentType.word())
                    .suggests(this::suggestAudioPlayback)
                    .executes(this::setAudioPlayback))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestAudioPlayback(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Arrays.stream(AudioPlayback.values()).forEach(playback -> builder.suggest(playback.name()));
    return builder.buildFuture();
  }

  private int setAudioPlayback(final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String argument = context.getArgument("playback", String.class);
    final Optional<AudioPlayback> optional = AudioPlayback.ofKey(argument);

    if (handleEmptyOptional(audience, Locale.ERR_INVALID_AUDIO_PLAYBACK.build(argument), optional)) {
      return SINGLE_SUCCESS;
    }

    this.handleAudioType(audience, optional.get());

    audience.sendMessage(Locale.SET_AUDIO_PLAYBACK.build(argument));

    return SINGLE_SUCCESS;
  }

  private void handleAudioType(
      @NotNull final Audience audience,
      @NotNull final AudioPlayback type) {
    switch (type) {
      case RESOURCEPACK -> this.setPackMode(audience);
      case DISCORD -> this.setDiscordMode(audience);
      case HTTP -> this.setHttpServerMode(audience);
      default -> throw new IllegalArgumentException("Audio playback is invalid!");
    }
  }

  private void setPackMode(@NotNull final Audience audience) {
    this.config.setAudioPlayback(AudioPlayback.RESOURCEPACK);
  }


  private void setDiscordMode(@NotNull final Audience audience) {

    if (handleNull(audience, Locale.ERR_INVALID_DISCORD_BOT.build(), this.plugin.getMediaBot())) {
      return;
    }

    if (true) { // temporary placeholder as I fix the bot
      return;
    }

    this.config.setAudioPlayback(AudioPlayback.DISCORD);
  }

  private void setHttpServerMode(@NotNull final Audience audience) {

    if (handleNull(audience, Locale.ERR_HTTP_AUDIO.build(), this.plugin.getHttpServer())) {
      return;
    }

    this.config.setAudioPlayback(AudioPlayback.HTTP);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
