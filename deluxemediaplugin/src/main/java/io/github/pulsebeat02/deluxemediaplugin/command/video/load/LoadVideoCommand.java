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
package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioPlayback;
import io.github.pulsebeat02.deluxemediaplugin.config.ServerInfo;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioOutputBuilder;
import io.github.pulsebeat02.ezmediacore.extraction.AudioAttributes;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LoadVideoCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  private final Path videoFolder;

  public LoadVideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.videoFolder = this.plugin.getDataFolder().resolve("videos");
    this.node =
        this.literal("load")
            .requires(has("deluxemediaplugin.command.video.load"))
            .then(new LoadUrlCommand(plugin, config, this).getNode())
            .then(new LoadMrlCommand(plugin, config, this).getNode())
            .then(new LoadPathCommand(plugin, config, this).getNode())
            .then(new LoadDeviceCommand(plugin, config, this).getNode())
            .then(new LoadDesktopCommand(plugin, config, this).getNode())
            .then(new LoadWindowCommand(plugin, config, this).getNode())
            .build();
  }

  public void loadVideo(@NotNull final Audience audience) {

    audience.sendMessage(Locale.LOAD_VIDEO.build());

    this.createFolders();
    this.cancelStream();

    final CompletableFuture<Void> future =
        CompletableFuture.runAsync(() -> this.handleVideo(audience), RESOURCE_WRAPPER_EXECUTOR);
    future.handle(Throwing.THROWING_FUTURE);

    this.config.setTask(future);
  }

  private void handleVideo(@NotNull final Audience audience) {
    final Input input = this.config.getMedia();
    if (this.handleUrlInput(audience, input)) {
      return;
    }
    if (this.handleResourcepackAudio(audience)) {
      return;
    }
    audience.sendMessage(Locale.LOADED_MEDIA.build(input.getInput()));
    this.config.setTask(null);
  }

  private boolean handleResourcepackAudio(@NotNull final Audience audience) {
    if (this.isResourcepackAudio()) {
      audience.sendMessage(Locale.RESOURCEPACK_CREATION.build());
      return this.handleResourcepack();
    }
    return false;
  }

  private boolean handleResourcepack() {
    final SoundKey key = SoundKey.ofSound("emc");
    final AudioAttributes attributes = AudioAttributes.OGG_CONFIGURATION;
    final ServerInfo info = this.plugin.getHttpAudioServer();
    final String ip = info.getIp();
    final int port = info.getPort();
    this.config.setAudioOutput(
        AudioOutputBuilder.pack().key(key).audio(attributes).host(ip).port(port));
    return false;
  }

  private boolean isResourcepackAudio() {
    return this.config.getAudioPlayback() == AudioPlayback.RESOURCEPACK;
  }

  private boolean handleUrlInput(@NotNull final Audience audience, @NotNull final Input input) {
    if (this.isUrlInput(input)) {
      return this.checkStreamMrl(audience, input);
    }
    return false;
  }

  private boolean checkStreamMrl(@NotNull final Audience audience, @NotNull final Input input) {
    try {
      return this.discoverUrl(audience, input);
    } catch (final IllegalArgumentException e) {
      audience.sendMessage(Locale.INVALID_INPUT.build());
      return true;
    }
  }

  private boolean discoverUrl(@NotNull final Audience audience, @NotNull final Input input) {
    final boolean stream = this.isStream(input);
    if (stream) {
      return this.handleStream(audience, input);
    } else {
      return this.handleUrl(audience, input);
    }
  }

  private boolean handleUrl(@NotNull final Audience audience, @NotNull final Input input) {
    return this.checkInvalidUrl(audience, input);
  }

  private boolean isStream(@NotNull final Input input) {
    final MediaRequest request = RequestUtils.requestMediaInformation(input);
    return request.isStream();
  }

  private boolean checkInvalidUrl(@NotNull final Audience audience, @NotNull final Input input) {
    final MediaRequest request = RequestUtils.requestMediaInformation(input);
    return handleTrue(audience, Locale.INVALID_INPUT.build(), request.getVideoLinks().isEmpty());
  }

  private boolean handleStream(@NotNull final Audience audience, @NotNull final Input input) {
    final boolean invalid = this.checkInvalidAudioPlayback(audience);
    if (invalid) {
      return true;
    }
    audience.sendMessage(Locale.LOADED_MEDIA.build(input.getInput()));
    return true;
  }

  private boolean checkInvalidAudioPlayback(@NotNull final Audience audience) {
    final boolean resourcepack = this.isResourcepackAudio();
    return handleTrue(audience, Locale.INVALID_STREAM_AUDIO_OUTPUT.build(), resourcepack);
  }

  private boolean isUrlInput(@NotNull final Input input) {
    return input instanceof UrlInput;
  }

  private void createFolders() {
    FileUtils.createDirectoryIfNotExistsExceptionally(this.videoFolder);
  }

  private void cancelStream() {
    final EnhancedExecution stream = this.config.getStream();
    Nill.ifNot(stream, () -> Try.closeable(stream));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
