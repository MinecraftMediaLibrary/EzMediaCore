/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    if (handleEmptyOptional(audience, Locale.INVALID_PLAYER_ALGORITHM.build(player), setting)) {
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
    return handleTrue(audience, Locale.VLC_UNSUPPORTED.build(), !supported && vlc);
  }

  private void setDitheringAlgorithm(@NotNull final PlayerAlgorithm setting) {
    this.config.setPlayerAlgorithm(setting);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
