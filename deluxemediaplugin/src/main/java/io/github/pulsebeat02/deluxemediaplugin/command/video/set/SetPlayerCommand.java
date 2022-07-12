package io.github.pulsebeat02.deluxemediaplugin.command.video.set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.PlayerAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetPlayerCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetPlayerCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("player")
            .requires(has("deluxemediaplugin.command.video.set.player"))
            .then(
                this.argument("player", StringArgumentType.word())
                    .suggests(this::suggestPlayerAlgorithm)
                    .executes(this::setPlayer))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestPlayerAlgorithm(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Stream.of(PlayerAlgorithm.values()).forEach(algorithm -> builder.suggest(algorithm.name()));
    return builder.buildFuture();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private int setPlayer(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String player = context.getArgument("player", String.class);
    final Optional<PlayerAlgorithm> setting = PlayerAlgorithm.ofKey(player);

    if (handleEmptyOptional(audience, Locale.ERR_INVALID_PLAYER_ALGORITHM.build(player), setting)) {
      return SINGLE_SUCCESS;
    }

    final PlayerAlgorithm algorithm = setting.get();

    if (this.isValidPlayer(audience, algorithm)) {
      return SINGLE_SUCCESS;
    }

    this.setDitheringAlgorithm(algorithm);

    audience.sendMessage(Locale.SET_PLAYER_ALGORITHM.build(player));

    return SINGLE_SUCCESS;
  }

  private boolean isValidPlayer(
      @NotNull final Audience audience, @NotNull final PlayerAlgorithm setting) {
    final boolean supported = this.plugin.library().isVLCSupported();
    final boolean vlc = setting == PlayerAlgorithm.VLC;
    return handleTrue(audience, Locale.ERR_VLC_UNSUPPORTED.build(), !supported && vlc);
  }

  private void setDitheringAlgorithm(@NotNull final PlayerAlgorithm setting) {
    this.config.setPlayerAlgorithm(setting);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
