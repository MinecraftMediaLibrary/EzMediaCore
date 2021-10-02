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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    if (extractor != null) {
      this.cancelled = true;
      try {
        extractor.close();
      } catch (final Exception e) {
        e.printStackTrace();
      }
      this.task.cancel(true);
      this.attributes.setExtractor(null);
      this.task = null;
      audience.sendMessage(Locale.CANCELLED_VIDEO_PROCESSING.build());
    } else {
      audience.sendMessage(Locale.ERR_CANCELLATION_VIDEO_PROCESSING.build());
    }
    return SINGLE_SUCCESS;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final Path folder = this.plugin.getBootstrap().getDataFolder().toPath().resolve("emc");
    final AtomicBoolean successful = new AtomicBoolean(true);
    final AtomicBoolean status = this.attributes.getCompletion();
    final Audience console = this.plugin.getLogger();
    this.attributes.cancelCurrentStream();
    CompletableFuture.runAsync(
            () -> {
              try {
                audience.sendMessage(Locale.LOADING_VIDEO.build());
                if (this.checkStream(audience, mrl)) {
                  successful.set(false);
                  return;
                }
                this.attributes.setVideoMrl(MrlConfiguration.ofMrl(mrl));
                if (this.attributes.getAudioOutputType() == AudioOutputType.RESOURCEPACK) {
                  status.set(false);
                  console.sendMessage(Locale.CREATE_RESOURCEPACK.build());
                  final Optional<Path> download = this.downloadMrl(audience, folder, mrl);
                  if (download.isEmpty()) {
                    this.plugin.getLogger().sendMessage(Locale.ERR_DOWNLOAD_VIDEO.build());
                    status.set(true);
                    successful.set(false);
                    return;
                  }
                  this.loadAndSendResourcepack(folder, download.get());
                  status.set(true);
                }
              } catch (final IOException | InterruptedException e) {
                console.sendMessage(Locale.ERR_LOAD_VIDEO.build());
                e.printStackTrace();
              }
            })
        .thenRun(
            () -> {
              if (successful.get()) {
                this.afterDownloadExecutionFinish(audience, mrl);
              }
            });

    return SINGLE_SUCCESS;
  }

  private void loadAndSendResourcepack(@NotNull final Path folder, @NotNull final Path download)
      throws IOException {
    final Path oggOutput = folder.resolve("output.ogg");
    final HttpServer daemon = this.plugin.getHttpServer();
    final FFmpegAudioExtractor extractor =
        new FFmpegAudioExtractor(
            this.plugin.library(), this.plugin.getAudioConfiguration(), download, oggOutput);
    this.attributes.setExtractor(extractor);
    extractor.execute();
    final ResourcepackSoundWrapper wrapper =
        new ResourcepackSoundWrapper(
            daemon.getDaemon().getServerPath().resolve("resourcepack.zip"),
            "Audio Resourcepack",
            PackFormat.getCurrentFormat().getId());
    wrapper.addSound(this.plugin.getBootstrap().getName().toLowerCase(java.util.Locale.ROOT),
        oggOutput);
    wrapper.wrap();
    this.attributes.setOggMrl(MrlConfiguration.ofMrl(oggOutput));
    final Path path = wrapper.getResourcepackFilePath();
    this.attributes.setResourcepackUrl(daemon.createUrl(path));
    this.attributes.setResourcepackHash(
        HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));
  }

  private @NotNull Optional<Path> downloadMrl(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final String mrl)
      throws IOException, InterruptedException {
    final Path downloadPath;
    if (PathUtils.isValidPath(mrl)) {
      downloadPath = Path.of(mrl);
    } else {
      final List<String> videoMrls = RequestUtils.getAudioURLs(mrl);
      if (videoMrls.isEmpty()) {
        audience.sendMessage(Locale.ERR_INVALID_MRL.build());
        return Optional.empty();
      }
      downloadPath = RequestUtils.downloadFile(folder.resolve("temp-audio"), videoMrls.get(0));
    }
    return Optional.of(downloadPath);
  }

  private boolean checkStream(@NotNull final Audience audience, @NotNull final String mrl) {
    if (RequestUtils.isStream(mrl)) {
      if (this.attributes.getAudioOutputType() == AudioOutputType.RESOURCEPACK) {
        audience.sendMessage(Locale.ERR_INVALID_AUDIO_OUTPUT.build());
      } else {
        audience.sendMessage(Locale.LOADED_MEDIA.build(mrl));
      }
      this.attributes.getCompletion().set(true);
      this.cancelled = false;
      this.attributes.setVideoMrl(MrlConfiguration.ofMrl(mrl));
      return true;
    } else {
      final List<String> urls = RequestUtils.getVideoURLs(mrl);
      if (urls.size() == 1 && urls.get(0).equals(mrl)) {
        audience.sendMessage(Locale.ERR_INVALID_MRL.build());
      }
    }
    return false;
  }

  private void afterDownloadExecutionFinish(
      @NotNull final Audience audience, @NotNull final String mrl) {
    this.sendCompletionMessage(audience, mrl);
    this.attributes.getCompletion().set(true);
    this.cancelled = false;
  }

  private void sendCompletionMessage(@NotNull final Audience audience, @NotNull final String mrl) {
    if (!this.cancelled) {
      ResourcepackUtils.forceResourcepackLoad(
          this.plugin.library(),
          Bukkit.getOnlinePlayers(),
          this.attributes.getResourcepackUrl(),
          this.attributes.getResourcepackHash());
      Bukkit.getOnlinePlayers().forEach((player) -> this.plugin.audience().player(player)
          .sendMessage(Locale.SEND_RESOURCEPACK_URL.build(player)));
      audience.sendMessage(Locale.LOADED_MEDIA.build(mrl));
    }
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
    if (this.isPlayer(audience, sender)) {
      return SINGLE_SUCCESS;
    }
    final String url = this.attributes.getResourcepackUrl();
    final byte[] hash = this.attributes.getResourcepackHash();
    ResourcepackUtils.forceResourcepackLoad(
        this.plugin.library(),
        entities.stream().map(entity -> (Player) entity).collect(Collectors.toSet()),
        url,
        hash);
    audience.sendMessage(Locale.SENT_RESOURCEPACK.build(url, hash));
    return SINGLE_SUCCESS;
  }

  private boolean checkNonPlayer(
      @NotNull final Audience audience, @NotNull final List<Entity> entities) {
    if (entities.parallelStream().anyMatch(entity -> !(entity instanceof Player))) {
      audience.sendMessage(Locale.ERR_INVALID_TARGET_SELECTOR.build());
      return true;
    }
    return false;
  }

  private boolean isPlayer(@NotNull final Audience audience, @NotNull final CommandSender sender) {
    if (!(sender instanceof Player)) {
      audience.sendMessage(Locale.ERR_PLAYER_SENDER.build());
      return true;
    }
    return false;
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    if (this.attributes.getResourcepackUrl() == null
        && this.attributes.getResourcepackHash() == null) {
      audience.sendMessage(Locale.ERR_NO_RESOURCEPACK.build());
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
