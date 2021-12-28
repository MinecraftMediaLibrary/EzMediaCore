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
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.requiresPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio.AudioOutputType;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.io.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VideoLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;
  private CompletableFuture<Void> task;
  private volatile boolean cancelled;

  public VideoLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("load")
            .then(this.argument("mrl", StringArgumentType.greedyString()).executes(this::loadVideo))
            .then(
                this.literal("resourcepack")
                    .then(
                        this.argument("targets", StringArgumentType.greedyString())
                            .executes(this::sendResourcepack)))
            .then(this.literal("cancel-download").executes(this::cancelDownload))
            .build();
  }

  private int cancelDownload(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final EnhancedExecution extractor = this.attributes.getExtractor();

    if (handleNull(audience, Locale.ERR_CANCELLATION_VIDEO_PROCESSING.build(), extractor)) {
      return SINGLE_SUCCESS;
    }

    this.setupCancelledAttributes(extractor, audience);

    return SINGLE_SUCCESS;
  }

  private void setupCancelledAttributes(
      @Nullable final EnhancedExecution extractor, @NotNull final Audience audience) {

    Nill.ifNot(extractor, () -> this.cancelExtractor(extractor));
    Nill.ifNot(this.task, this::cancelTask);

    audience.sendMessage(Locale.CANCELLED_VIDEO_PROCESSING.build());
  }

  private void cancelTask() {
    this.task.cancel(true);
    this.task = null;
  }

  private void cancelExtractor(@Nullable final EnhancedExecution extractor) {
    this.cancelled = true;
    this.closeExtractor(extractor);
    this.attributes.setExtractor(null);
  }

  private void closeExtractor(@NotNull final EnhancedExecution extractor) {
    Try.closeable(extractor);
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final AtomicBoolean successful = new AtomicBoolean(true);

    this.attributes.cancelCurrentStream();

    CompletableFuture.runAsync(
            () -> this.handleVideoLoad(audience, successful, mrl), RESOURCE_WRAPPER_EXECUTOR)
        .thenRun(() -> this.sendCompletionMessage(audience, mrl, successful))
        .handle(Throwing.THROWING_FUTURE);

    return SINGLE_SUCCESS;
  }

  private void handleVideoLoad(
      @NotNull final Audience audience,
      @NotNull final AtomicBoolean successful,
      @NotNull final String mrl) {

    final Path folder = this.plugin.getBootstrap().getDataFolder().toPath().resolve("emc");
    final AtomicBoolean status = this.attributes.getCompletion();
    final Audience console = this.plugin.getConsoleAudience();

    audience.sendMessage(Locale.LOADING_VIDEO.build());

    if (!PathUtils.isValidPath(mrl) && this.checkStream(audience, mrl)) {
      successful.set(false);
      return;
    }

    this.attributes.setVideoMrl(MrlConfiguration.ofMrl(mrl));

    try {
      if (this.attributes.getAudioOutputType() == AudioOutputType.RESOURCEPACK) {
        this.loadResourcepackVideo(audience, status, successful, folder, mrl);
      }
    } catch (final IOException | InterruptedException e) {
      console.sendMessage(Locale.ERR_LOAD_VIDEO.build());
      throw new AssertionError(e);
    }
  }

  private void loadResourcepackVideo(
      @NotNull final Audience audience,
      @NotNull final AtomicBoolean status,
      @NotNull final AtomicBoolean successful,
      @NotNull final Path folder,
      @NotNull final String mrl)
      throws IOException, InterruptedException {

    status.set(false);

    this.plugin.getConsoleAudience().sendMessage(Locale.CREATE_RESOURCEPACK.build());

    final Optional<Path> download = this.downloadMrl(audience, folder, mrl);
    if (download.isEmpty()) {
      this.plugin.getConsoleAudience().sendMessage(Locale.ERR_DOWNLOAD_VIDEO.build());
      status.set(true);
      successful.set(false);
      return;
    }

    this.loadAndSendResourcepack(folder, download.get());

    status.set(true);
  }

  private void loadAndSendResourcepack(@NotNull final Path folder, @NotNull final Path download)
      throws IOException {
    final Path oggOutput = folder.resolve("output.ogg");
    this.executeFFmpegExtractor(download, oggOutput);
    this.executeResourcepackWrapper(oggOutput);
  }

  private void executeFFmpegExtractor(@NotNull final Path download, @NotNull final Path oggOutput) {

    final AudioConfiguration configuration = this.plugin.getAudioConfiguration();
    final FFmpegAudioExtractor extractor =
        FFmpegAudioExtractor.ofFFmpegAudioExtractor(
            this.plugin.library(), configuration, download, oggOutput);

    this.attributes.setExtractor(extractor);

    extractor.execute();
  }

  private void executeResourcepackWrapper(@NotNull final Path oggOutput) throws IOException {
    final ResourcepackSoundWrapper wrapper = this.executeResourcepackSoundWrapper(oggOutput);
    this.setResourcepackAttributes(wrapper, oggOutput);
  }

  private @NotNull ResourcepackSoundWrapper executeResourcepackSoundWrapper(
      @NotNull final Path oggOutput) throws IOException {

    final HttpServer daemon = this.startDaemon();
    final Path target = daemon.getDaemon().getServerPath().resolve("resourcepack.zip");
    final int id = PackFormat.getCurrentFormat().getId();
    final String sound = this.plugin.getBootstrap().getName().toLowerCase(java.util.Locale.ROOT);

    final ResourcepackSoundWrapper wrapper =
        ResourcepackSoundWrapper.ofSoundPack(target, "Audio Resourcepack", id);
    wrapper.addSound(sound, oggOutput);
    wrapper.wrap();

    return wrapper;
  }

  private void setResourcepackAttributes(
      @NotNull final ResourcepackSoundWrapper wrapper, @NotNull final Path oggOutput) {

    final HttpServer daemon = this.startDaemon();

    this.attributes.setOggMrl(MrlConfiguration.ofMrl(oggOutput));

    final Path path = wrapper.getResourcepackFilePath();
    final String url = daemon.createUrl(path);
    final byte[] hash = HashingUtils.createHashSha1(path);

    this.attributes.setPackUrl(url);
    this.attributes.setPackHash(hash);
  }

  @NotNull
  private HttpServer startDaemon() {
    final HttpServer daemon = this.plugin.getHttpServer();
    if (!daemon.isRunning()) {
      daemon.startServer();
    }
    return daemon;
  }

  private @NotNull Optional<Path> downloadMrl(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final String mrl)
      throws IOException, InterruptedException {
    return PathUtils.isValidPath(mrl)
        ? Optional.of(Path.of(mrl))
        : this.getDownloadedMrl(audience, folder, mrl);
  }

  private Optional<Path> getDownloadedMrl(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final String mrl)
      throws IOException, InterruptedException {

    final List<MrlConfiguration> videoMrls = RequestUtils.getAudioURLs(MrlConfiguration.ofMrl(mrl));

    if (handleTrue(audience, Locale.ERR_INVALID_MRL.build(), videoMrls.isEmpty())) {
      return Optional.empty();
    }

    final Path target =
        RequestUtils.downloadFile(folder.resolve("temp-audio"), videoMrls.get(0).getMrl());

    return Optional.of(target);
  }

  private boolean checkStream(@NotNull final Audience audience, @NotNull final String mrl) {

    final boolean stream;
    try {
      stream = RequestUtils.isStream(MrlConfiguration.ofMrl(mrl));
    } catch (final IllegalArgumentException e) {
      audience.sendMessage(Locale.ERR_INVALID_MRL.build());
      return true;
    }

    if (stream) {
      this.setStreamAttributes(audience, mrl);
      return true;
    } else {
      this.checkInvalidMrl(audience, mrl);
    }

    return false;
  }

  private void setStreamAttributes(@NotNull final Audience audience, @NotNull final String mrl) {

    if (handleTrue(
        audience,
        Locale.ERR_INVALID_AUDIO_OUTPUT.build(),
        this.attributes.getAudioOutputType() == AudioOutputType.RESOURCEPACK)) {
      return;
    }

    audience.sendMessage(Locale.LOADED_MEDIA.build(mrl));

    this.attributes.getCompletion().set(true);
    this.cancelled = false;
    this.attributes.setVideoMrl(MrlConfiguration.ofMrl(mrl));
  }

  private void checkInvalidMrl(@NotNull final Audience audience, @NotNull final String mrl) {

    final List<MrlConfiguration> urls = RequestUtils.getVideoURLs(MrlConfiguration.ofMrl(mrl));
    final boolean equal = urls.get(0).getMrl().equals(mrl);
    final boolean size = urls.size() == 1;

    handleTrue(audience, Locale.ERR_INVALID_MRL.build(), size && equal);
  }

  private void afterDownloadExecutionFinish(
      @NotNull final Audience audience, @NotNull final String mrl) {
    this.sendCompletionMessage(audience, mrl);
    this.attributes.getCompletion().set(true);
    this.cancelled = false;
  }

  private void sendCompletionMessage(@NotNull final Audience audience, @NotNull final String mrl) {
    if (!this.cancelled) {

      final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
      final String url = this.attributes.getPackUrl();
      final byte[] hash = this.attributes.getPackHash();

      ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), players, url, hash);

      players.forEach(this::sendSeparatePackMessage);

      audience.sendMessage(Locale.LOADED_MEDIA.build(mrl));
    }
  }

  private void sendCompletionMessage(
      @NotNull final Audience audience,
      @NotNull final String mrl,
      @NotNull final AtomicBoolean successful) {
    if (successful.get()) {
      this.afterDownloadExecutionFinish(audience, mrl);
    }
  }

  private void sendSeparatePackMessage(@NotNull final Player player) {
    final Audience playerAudience = this.plugin.audience().player(player);
    playerAudience.sendMessage(Locale.SEND_RESOURCEPACK_URL.build(player));
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final Audience audience = this.plugin.audience().sender(sender);
    final String targets = context.getArgument("targets", String.class);
    final List<Entity> entities =
        this.plugin.getBootstrap().getServer().selectEntities(sender, targets);
    if (this.checkNonPlayer(audience, entities)) {
      return SINGLE_SUCCESS;
    }
    if (this.unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }
    if (requiresPlayer(this.plugin, sender)) {
      return SINGLE_SUCCESS;
    }

    this.forceResourcepack(entities, audience);

    return SINGLE_SUCCESS;
  }

  private void forceResourcepack(
      @NotNull final List<Entity> entities, @NotNull final Audience audience) {

    final String url = this.attributes.getPackUrl();
    final byte[] hash = this.attributes.getPackHash();
    final Set<Player> cast =
        entities.stream().map(entity -> (Player) entity).collect(Collectors.toSet());

    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), cast, url, hash);

    audience.sendMessage(Locale.SENT_RESOURCEPACK.build(url, hash));
  }

  private boolean checkNonPlayer(
      @NotNull final Audience audience, @NotNull final List<Entity> entities) {
    final boolean assertion = entities.stream().anyMatch(entity -> entity instanceof Player);
    return handleFalse(audience, Locale.ERR_INVALID_TARGET_SELECTOR.build(), assertion);
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {

    final Component component = Locale.ERR_NO_RESOURCEPACK.build();

    if (handleNull(audience, component, this.attributes.getPackUrl())) {
      return true;
    }

    return handleNull(audience, component, this.attributes.getPackHash());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
