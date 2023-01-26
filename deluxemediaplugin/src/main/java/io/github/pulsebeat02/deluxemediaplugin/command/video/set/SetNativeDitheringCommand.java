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
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.DitheringAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetNativeDitheringCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetNativeDitheringCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("native-dithering")
            .requires(has("deluxemediaplugin.command.video.set.nativedithering"))
            .then(
                this.argument("enabled", BoolArgumentType.bool())
                    .suggests(this::suggestNativeDithering)
                    .executes(this::setNativeDithering))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestNativeDithering(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    builder.suggest("true");
    builder.suggest("false");
    return builder.buildFuture();
  }

  private int setNativeDithering(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final boolean enabled = context.getArgument("enabled", Boolean.TYPE);

    if (this.checkInvalidDithering(audience)) {
      return SINGLE_SUCCESS;
    }

    this.setEnabled(enabled);

    final DitheringAlgorithm algorithm = this.config.getDitheringAlgorithm();
    audience.sendMessage(Locale.SET_NATIVE_DITHERING.build(algorithm, enabled));

    return SINGLE_SUCCESS;
  }

  private void setEnabled(final boolean enabled) {
    this.config.setNativeDithering(enabled);
  }

  private boolean checkInvalidDithering(@NotNull final Audience audience) {
    final DitheringAlgorithm algorithm = this.config.getDitheringAlgorithm();
    final boolean supported = algorithm.isNativelySupported();
    return handleTrue(
        audience, Locale.INVALID_NATIVE_DITHERING_ALGORITHM.build(algorithm), supported);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
