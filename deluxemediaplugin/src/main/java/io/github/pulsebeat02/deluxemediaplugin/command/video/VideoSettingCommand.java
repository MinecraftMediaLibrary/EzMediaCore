/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.gold;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherSetting;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class VideoSettingCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public VideoSettingCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("set")
            .then(
                this.literal("screen-dimension")
                    .then(
                        this.argument("screen-dimensions", StringArgumentType.greedyString())
                            .executes(this::setScreenDimensions)))
            .then(
                this.literal("itemframe-dimension")
                    .then(
                        this.argument("itemframe-dimensions", StringArgumentType.greedyString())
                            .executes(this::setItemframeDimensions)))
            .then(
                this.literal("starting-map")
                    .then(
                        this.argument(
                                "map-id",
                                IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
                            .executes(this::setStartingMap)))
            .then(
                this.literal("dither")
                    .then(
                        this.argument("dithering-option", StringArgumentType.word())
                            .suggests(this::suggestDitheringOptions)
                            .executes(this::setDitherMode)))
            .then(
                this.literal("mode")
                    .then(
                        this.argument("video-mode", StringArgumentType.word())
                            .suggests(this::suggestVideoModes)
                            .executes(this::setVideoMode)))
            .then(
                this.literal("audio-output")
                    .then(
                        this.argument("audio-type", StringArgumentType.word())
                            .suggests(this::suggestAudioOutputs)
                            .executes(this::setAudioOutput)))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestDitheringOptions(
      final CommandContext<CommandSender> context, final @NotNull SuggestionsBuilder builder) {
    Arrays.stream(DitherSetting.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private @NotNull CompletableFuture<Suggestions> suggestVideoModes(
      final CommandContext<CommandSender> context, final @NotNull SuggestionsBuilder builder) {
    Arrays.stream(PlaybackType.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private @NotNull CompletableFuture<Suggestions> suggestAudioOutputs(
      final CommandContext<CommandSender> context, final @NotNull SuggestionsBuilder builder) {
    Arrays.stream(AudioOutputType.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private int setAudioOutput(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    final String argument = context.getArgument("audio-type", String.class);
    final Optional<AudioOutputType> optional = AudioOutputType.ofKey(argument);
    if (optional.isEmpty()) {
      red(audience, "Could not find audio type %s".formatted(argument));
      return SINGLE_SUCCESS;
    }

    switch (optional.get()) {
      case RESOURCEPACK -> this.attributes.setAudioOutputType(AudioOutputType.RESOURCEPACK);
      case DISCORD -> {
        if (VideoCommandAttributes.TEMPORARY_PLACEHOLDER) {
          red(audience, "Unfortunately, Discord support is still being developed, as the Music API used is abandoned and we must find another solution!");
          return SINGLE_SUCCESS;
        }
        if (this.plugin.getMediaBot() == null) {
          red(audience, "Discord bot information provided in bot.yml is invalid!");
          return SINGLE_SUCCESS;
        }
        this.attributes.setAudioOutputType(AudioOutputType.DISCORD);
      }
      case HTTP -> {
        if (this.plugin.getHttpAudioServer() == null) {
          red(audience, "HTTP audio information provided in httpaudio.yml is invalid!");
          return SINGLE_SUCCESS;
        }
        this.attributes.setAudioOutputType(AudioOutputType.HTTP);
      }
      default -> throw new IllegalArgumentException("Audio type is invalid!");
    }

    gold(audience, "Successfully set the audio type to %s".formatted(argument));

    return SINGLE_SUCCESS;
  }

  private int setScreenDimensions(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtils.checkDimensionBoundaries(
            audience, context.getArgument("screen-dimensions", String.class));
    if (optional.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final int[] dimensions = optional.get();
    this.attributes.setPixelWidth(dimensions[0]);
    this.attributes.setPixelHeight(dimensions[1]);

    audience.sendMessage(
        format(
            join(
                noSeparators(),
                text("Set screen dimensions to ", GOLD),
                text(
                    "%d:%d "
                        .formatted(
                            this.attributes.getPixelWidth(), this.attributes.getPixelHeight()),
                    AQUA),
                text("(width:height)", GOLD))));

    return SINGLE_SUCCESS;
  }

  private int setItemframeDimensions(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtils.checkDimensionBoundaries(
            audience, context.getArgument("itemframe-dimensions", String.class));

    if (optional.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final int[] dims = optional.get();
    this.attributes.setFrameWidth(dims[0]);
    this.attributes.setFrameHeight(dims[1]);

    audience.sendMessage(
        format(
            join(
                noSeparators(),
                text("Set itemframe map dimensions to ", GOLD),
                text(
                    "%s:%s "
                        .formatted(
                            this.attributes.getFrameWidth(), this.attributes.getFrameHeight()),
                    AQUA),
                text("(width:height)", GOLD))));

    return SINGLE_SUCCESS;
  }

  private int setStartingMap(@NotNull final CommandContext<CommandSender> context) {

    this.attributes.setMap(context.getArgument("map-id", int.class));

    this.plugin
        .audience()
        .sender(context.getSource())
        .sendMessage(
            format(
                join(
                    noSeparators(),
                    text("Set starting map id to ", GOLD),
                    text(this.attributes.getMap(), AQUA))));

    return SINGLE_SUCCESS;
  }

  private int setDitherMode(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String algorithm = context.getArgument("dithering-option", String.class);
    final Optional<DitherSetting> setting = DitherSetting.ofKey(algorithm);

    if (setting.isEmpty()) {
      red(audience, "Could not find dither type %s".formatted(algorithm));
    } else {
      this.attributes.setDitherType(setting.get());
      audience.sendMessage(
          format(join(noSeparators(), text("Set dither type to ", GOLD), text(algorithm, AQUA))));
    }

    return SINGLE_SUCCESS;
  }

  private int setVideoMode(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mode = context.getArgument("video-mode", String.class);
    final Optional<PlaybackType> type = PlaybackType.ofKey(mode);

    if (type.isEmpty()) {
      red(audience, "Could not find video mode %s".formatted(mode));
    } else {
      this.attributes.setVideoType(type.get());
      audience.sendMessage(
          format(join(noSeparators(), text("Set video mode to ", GOLD), text(mode, AQUA))));
    }

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
