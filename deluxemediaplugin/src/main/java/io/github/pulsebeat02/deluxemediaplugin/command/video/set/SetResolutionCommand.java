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
import static io.github.pulsebeat02.deluxemediaplugin.utility.component.ChatUtils.checkDimensionBoundaries;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X1280_720;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X1366_768;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X1440_900;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X1536_864;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X1920_1080;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X360_640;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X360_780;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X375_667;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X375_812;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X3840_2160;
import static io.github.pulsebeat02.ezmediacore.dimension.PixelDimension.X414_896;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetResolutionCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetResolutionCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("resolution")
            .requires(has("deluxemediaplugin.command.video.set.resolution"))
            .then(
                this.argument("resolution", StringArgumentType.greedyString())
                    .suggests(this::suggestVideoResolution)
                    .executes(this::setVideoResolution))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestVideoResolution(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    final Set<Dimension> dimensions =
        Set.of(
            X360_640,
            X375_667,
            X414_896,
            X360_780,
            X375_812,
            X1366_768,
            X1920_1080,
            X1536_864,
            X1440_900,
            X1280_720,
            X3840_2160);
    for (final Dimension dimension : dimensions) {
      builder.suggest("%s:%s".formatted(dimension.getWidth(), dimension.getHeight()));
    }
    return builder.buildFuture();
  }

  private int setVideoResolution(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        checkDimensionBoundaries(audience, context.getArgument("resolution", String.class));
    if (optional.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final int[] dimensions = optional.get();
    final int width = dimensions[0];
    final int height = dimensions[1];

    this.setResolution(width, height);

    audience.sendMessage(Locale.SET_RESOLUTION.build(width, height));

    return SINGLE_SUCCESS;
  }

  private void setResolution(final int width, final int height) {
    this.config.setResolutionWidth(width);
    this.config.setResolutionHeight(height);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
