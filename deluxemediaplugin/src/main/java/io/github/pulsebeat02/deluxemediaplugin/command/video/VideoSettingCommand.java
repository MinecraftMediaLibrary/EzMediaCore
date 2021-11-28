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
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherSetting;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioOutputType;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.PlaybackType;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.component.ChatUtils;
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

    if (handleEmptyOptional(audience, Locale.ERR_INVALID_AUDIO_TYPE.build(argument), optional)) {
      return SINGLE_SUCCESS;
    }

    this.handleAudioType(audience, optional.get());

    audience.sendMessage(Locale.SET_AUDIO_TYPE.build(argument));

    return SINGLE_SUCCESS;
  }

  private void handleAudioType(
      @NotNull final Audience audience,
      @NotNull final AudioOutputType type) {
    switch (type) {
      case RESOURCEPACK -> this.setPackMode(audience);
      case DISCORD -> this.setDiscordMode(audience);
      case HTTP -> this.setHttpServerMode(audience);
      default -> throw new IllegalArgumentException("Audio type is invalid!");
    }
  }

  private void setPackMode(@NotNull final Audience audience) {
    this.attributes.setAudioOutputType(AudioOutputType.RESOURCEPACK);
  }


  private void setDiscordMode(@NotNull final Audience audience) {

    if (handleTrue(audience, Locale.ERR_DEVELOPMENT_FEATURE.build(), VideoCommandAttributes.TEMPORARY_PLACEHOLDER)
        || handleNull(audience, Locale.ERR_INVALID_DISCORD_BOT.build(), this.plugin.getMediaBot())) {
      return;
    }

    this.attributes.setAudioOutputType(AudioOutputType.DISCORD);
  }

  private void setHttpServerMode(@NotNull final Audience audience) {

    if (handleNull(audience, Locale.ERR_HTTP_AUDIO.build(), this.plugin.getHttpServer())) {
      return;
    }

    this.attributes.setAudioOutputType(AudioOutputType.HTTP);
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
    final int width = dimensions[0];
    final int height = dimensions[1];

    this.attributes.setPixelWidth(width);
    this.attributes.setPixelHeight(height);

    audience.sendMessage(Locale.CHANGED_VIDEO_SCREEN_DIMS.build(width, height));

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
    final int width = dims[0];
    final int height = dims[1];

    this.attributes.setFrameWidth(width);
    this.attributes.setFrameHeight(height);

    audience.sendMessage(Locale.CHANGED_ITEMFRAME_DIMS.build(width, height));

    return SINGLE_SUCCESS;
  }

  private int setStartingMap(@NotNull final CommandContext<CommandSender> context) {

    final int id = context.getArgument("map-id", Integer.TYPE);
    final Audience audience = this.plugin.audience().sender(context.getSource());

    this.attributes.setMap(id);

    audience.sendMessage(Locale.CHANGED_VIDEO_MAP_ID.build(id));

    return SINGLE_SUCCESS;
  }

  private int setDitherMode(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String algorithm = context.getArgument("dithering-option", String.class);
    final Optional<DitherSetting> setting = DitherSetting.ofKey(algorithm);
    if (handleEmptyOptional(audience, Locale.ERR_INVALID_DITHER_TYPE.build(algorithm), setting)) {
      return SINGLE_SUCCESS;
    }

    this.attributes.setDitherType(setting.get());

    audience.sendMessage(Locale.SET_DITHER_TYPE.build(algorithm));

    return SINGLE_SUCCESS;
  }

  private int setVideoMode(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mode = context.getArgument("video-mode", String.class);
    final Optional<PlaybackType> type = PlaybackType.ofKey(mode);
    if (handleEmptyOptional(audience, Locale.ERR_INVALID_VIDEO_TYPE.build(mode), type)) {
      return SINGLE_SUCCESS;
    }

    this.attributes.setVideoType(type.get());

    audience.sendMessage(Locale.SET_VIDEO_TYPE.build(mode));

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
