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
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
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
      extractor.cancelProcess();
      this.task.cancel(true);
      this.attributes.setExtractor(null);
      this.task = null;
      gold(audience, "Successfully cancelled the video loading process!");
    } else {
      red(audience, "You aren't loading a video!");
    }
    return SINGLE_SUCCESS;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final Path folder = this.plugin.getBootstrap().getDataFolder().toPath().resolve("emc");
    final AtomicBoolean successful = new AtomicBoolean(true);

    CompletableFuture.runAsync(
            () -> {
              try {
                if (this.checkStream(audience, mrl)) {
                  successful.set(false);
                  return;
                }
                this.attributes.getCompletion().set(false);
                gold(
                    audience,
                    "Creating a resourcepack for audio. Depending on the length of the video, it make take some time.");
                this.attributes.setVideoMrl(MrlConfiguration.ofMrl(mrl));
                final Optional<Path> download = this.downloadMrl(audience, folder, mrl);
                if (download.isEmpty()) {
                  this.plugin.getBootstrap().getLogger().severe("Failed to download video!");
                  successful.set(false);
                  return;
                }
                this.loadAndSendResourcepack(folder, download.get());
              } catch (final IOException e) {
                this.plugin.getBootstrap().getLogger().severe("Failed to load video!");
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
    final HttpServer daemon = this.plugin.getHttpServer();
    final FFmpegAudioExtractor extractor =
        new FFmpegAudioExtractor(
            this.plugin.library(),
            this.plugin.getAudioConfiguration(),
            download,
            folder.resolve("output.ogg"));
    this.attributes.setExtractor(extractor);
    extractor.execute();
    final ResourcepackSoundWrapper wrapper =
        new ResourcepackSoundWrapper(
            daemon.getDaemon().getServerPath().resolve("resourcepack.zip"),
            "Video Audio",
            PackFormat.getCurrentFormat().getId());
    wrapper.addSound(
        this.plugin.getBootstrap().getName().toLowerCase(Locale.ROOT), extractor.getOutput());
    wrapper.wrap();
    final Path path = wrapper.getResourcepackFilePath();
    this.attributes.setOggMrl(MrlConfiguration.ofMrl(path));
    this.attributes.setResourcepackUrl(daemon.createUrl(path));
    this.attributes.setResourcepackHash(
        HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));
  }

  private @NotNull Optional<Path> downloadMrl(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final String mrl)
      throws IOException {
    final Path downloadPath;
    if (PathUtils.isValidPath(mrl)) {
      downloadPath = Path.of(mrl);
    } else {
      final List<String> videoMrls = RequestUtils.getVideoURLs(mrl);
      if (videoMrls.isEmpty()) {
        red(audience, "Invalid MRL link! Not supported!");
        return Optional.empty();
      }
      downloadPath = DependencyUtils.downloadFile(folder.resolve("media.mp4"), videoMrls.get(0));
    }
    return Optional.of(downloadPath);
  }

  private boolean checkStream(@NotNull final Audience audience, @NotNull final String mrl) {
    if (RequestUtils.isStream(mrl)) {
      if (this.attributes.getAudioOutputType() != AudioOutputType.DISCORD) {
        red(
            audience,
            "You cannot play streams without using Discord or a dynamic audio player with audio. Proceeding to play without audio.");
        this.attributes.setVideoMrl(MrlConfiguration.ofMrl(mrl));
        return true;
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
      Bukkit.getOnlinePlayers().parallelStream()
          .forEach(
              player ->
                  this.plugin
                      .audience()
                      .player(player)
                      .sendMessage(
                          text()
                              .append(text("Loaded resourcepack for all players! Click ", GOLD))
                              .append(
                                  text(
                                      "this message",
                                      style(
                                          AQUA,
                                          BOLD,
                                          UNDERLINED,
                                          runCommand(
                                              "/video load resourcepack %s"
                                                  .formatted(player.getName())),
                                          text("Click to get the resourcepack!", GOLD)
                                              .asHoverEvent())))
                              .append(text(" to retrieve the resourcepack", GOLD))
                              .build()));
      gold(audience, "Successfully loaded video %s".formatted(mrl));
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
    gold(audience, "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash)));
    return SINGLE_SUCCESS;
  }

  private boolean checkNonPlayer(
      @NotNull final Audience audience, @NotNull final List<Entity> entities) {
    if (entities.parallelStream().anyMatch(entity -> !(entity instanceof Player))) {
      red(audience, "The target selector you chose contains entities that aren't players!");
      return true;
    }
    return false;
  }

  private boolean isPlayer(@NotNull final Audience audience, @NotNull final CommandSender sender) {
    if (!(sender instanceof Player)) {
      red(audience, "You must be a player to execute this command!");
      return true;
    }
    return false;
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    if (this.attributes.getResourcepackUrl() == null
        && this.attributes.getResourcepackHash() == null) {
      audience.sendMessage(
          format(text("Please load a resourcepack before executing this command!", RED)));
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
