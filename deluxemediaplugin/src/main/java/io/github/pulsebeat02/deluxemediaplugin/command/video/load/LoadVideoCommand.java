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
package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.command.video.load.wrapper.SimpleResourcepackWrapper;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioPlayback;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.io.IOException;
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

    if (this.handleResourcepackAudio(audience, input)) {
      return;
    }

    this.sendCompletionMessage();

    audience.sendMessage(Locale.LOADED_MEDIA.build(input.getInput()));

    this.config.setTask(null);
  }

  private void sendCompletionMessage() {
    if (this.config.getAudioPlayback() == AudioPlayback.RESOURCEPACK) {
      final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
      final String url = this.config.getPackUrl();
      final byte[] hash = this.config.getPackHash();
      ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), players, url, hash);
      players.forEach(this::sendSeparatePackMessage);
    }
  }

  private void sendSeparatePackMessage(@NotNull final Player player) {
    final Audience playerAudience = this.plugin.audience().player(player);
    playerAudience.sendMessage(Locale.SEND_RESOURCEPACK_URL.build(player.getName()));
  }

  private boolean handleResourcepackAudio(
      @NotNull final Audience audience, @NotNull final Input input) {
    if (this.isResourcepackAudio()) {
      audience.sendMessage(Locale.RESOURCEPACK_CREATION.build());
      return this.handleResourcepack(audience, input);
    }
    return false;
  }

  private boolean handleResourcepack(@NotNull final Audience audience, @NotNull final Input input) {
    try {

      final Optional<String> download = this.getSourceInput(audience, input);
      if (download.isEmpty()) {
        audience.sendMessage(Locale.INVALID_EXTRACTION.build());
        return true;
      }

      new SimpleResourcepackWrapper(this.plugin, this.config, download.get(), this.videoFolder)
          .loadResourcepack();

    } catch (final IOException e) {
      audience.sendMessage(Locale.INVALID_VIDEO.build());
      e.printStackTrace();
    }
    return false;
  }

  private @NotNull Optional<String> getSourceInput(
      @NotNull final Audience audience, @NotNull final Input input) {
    return this.isPathInput(input)
        ? Optional.of(input.getInput())
        : this.getAudioSource(audience, input);
  }

  private boolean isPathInput(@NotNull final Input input) {
    return input instanceof PathInput;
  }

  private Optional<String> getAudioSource(
      @NotNull final Audience audience, @NotNull final Input input) {
    final MediaRequest request = RequestUtils.requestMediaInformation(input);
    final List<Input> results = request.getAudioLinks();
    if (this.checkInvalidUrl(audience, results)) {
      return Optional.empty();
    }
    final Input first = results.get(0);
    return Optional.of(first.getInput());
  }

  private boolean checkInvalidUrl(
      @NotNull final Audience audience, @NotNull final List<Input> results) {
    return handleTrue(audience, Locale.INVALID_INPUT.build(), results.isEmpty());
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
      return this.isStream(input)
          ? this.handleStream(audience, input)
          : this.checkInvalidUrl(audience, input);
    } catch (final IllegalArgumentException e) {
      audience.sendMessage(Locale.INVALID_INPUT.build());
      return true;
    }
  }

  private boolean isStream(@NotNull final Input input) {
    final MediaRequest request = RequestUtils.requestMediaInformation(input);
    return request.isStream();
  }

  private boolean checkInvalidUrl(@NotNull final Audience audience, @NotNull final Input input) {

    final MediaRequest request = RequestUtils.requestMediaInformation(input);
    final List<Input> urls = request.getVideoLinks();

    final boolean equal = urls.get(0).equals(input);
    final boolean size = urls.size() == 1;

    return handleTrue(audience, Locale.INVALID_INPUT.build(), size && equal);
  }

  private boolean handleStream(@NotNull final Audience audience, @NotNull final Input input) {

    if (this.checkInvalidAudioPlayback(audience)) {
      return true;
    }

    audience.sendMessage(Locale.LOADED_MEDIA.build(input.getInput()));

    return true;
  }

  private boolean checkInvalidAudioPlayback(@NotNull final Audience audience) {
    return handleTrue(
        audience, Locale.INVALID_STREAM_AUDIO_OUTPUT.build(), this.isResourcepackAudio());
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
