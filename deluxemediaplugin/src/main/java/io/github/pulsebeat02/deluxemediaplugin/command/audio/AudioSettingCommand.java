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
package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioOutputType;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AudioSettingCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public AudioSettingCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final AudioCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("set")
            .then(
                this.literal("output")
                    .then(
                        this.argument("output", StringArgumentType.greedyString())
                            .suggests(this::suggestAudioOutputs)
                            .executes(this::setAudioOutput)))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestAudioOutputs(
      final CommandContext<CommandSender> context, final @NotNull SuggestionsBuilder builder) {
    Arrays.stream(AudioOutputType.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private int setAudioOutput(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String argument = context.getArgument("audio", String.class);
    final Optional<AudioOutputType> optional = AudioOutputType.ofKey(argument);
    if (optional.isEmpty()) {
      audience.sendMessage(Locale.ERR_INVALID_AUDIO_TYPE.build(argument));
      return SINGLE_SUCCESS;
    }
    switch (optional.get()) {
      case RESOURCEPACK -> this.attributes.setAudioOutputType(AudioOutputType.RESOURCEPACK);
      case DISCORD -> {
        if (this.setDiscordMode(audience)) {
          return SINGLE_SUCCESS;
        }
      }
      case HTTP -> {
        if (this.setHttpServerMode(audience)) {
          return SINGLE_SUCCESS;
        }
      }
      default -> throw new IllegalArgumentException("Audio type is invalid!");
    }
    audience.sendMessage(Locale.SET_AUDIO_TYPE.build(argument));
    return SINGLE_SUCCESS;
  }

  private boolean setDiscordMode(@NotNull final Audience audience) {
    if (VideoCommandAttributes.TEMPORARY_PLACEHOLDER) {
      audience.sendMessage(Locale.ERR_DEVELOPMENT_FEATURE.build());
      return true;
    }
    if (this.plugin.getMediaBot() == null) {
      audience.sendMessage(Locale.ERR_INVALID_DISCORD_BOT.build());
      return true;
    }
    this.attributes.setAudioOutputType(AudioOutputType.DISCORD);
    return false;
  }

  private boolean setHttpServerMode(@NotNull final Audience audience) {
    if (this.plugin.getHttpAudioServer() == null) {
      audience.sendMessage(Locale.ERR_HTTP_AUDIO.build());
      return true;
    }
    this.attributes.setAudioOutputType(AudioOutputType.HTTP);
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
