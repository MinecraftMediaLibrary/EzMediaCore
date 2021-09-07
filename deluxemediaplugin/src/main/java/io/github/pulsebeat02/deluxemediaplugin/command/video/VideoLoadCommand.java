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
/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.external;
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
import io.github.pulsebeat02.ezmediacore.ffmpeg.SpotifyTrackExtractor;
import io.github.pulsebeat02.ezmediacore.ffmpeg.YoutubeVideoAudioExtractor;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VideoLoadCommand implements CommandSegment.Literal<CommandSender> {

  private static final TextComponent RESOURCEPACK_MESSAGE;
  private static final TextComponent HOVER_MESSAGE;

  static {
    HOVER_MESSAGE = Component.text("Click to get the resourcepack!", GOLD);
    RESOURCEPACK_MESSAGE = Component.text()
        .append(text("Loaded resourcepack for all players! Click ", GOLD))
        .append(text("this message",
            style(AQUA, BOLD, UNDERLINED, runCommand("/video load resourcepack"),
                HOVER_MESSAGE.asHoverEvent())))
        .append(text(" to retrieve the resourcepack", GOLD)).build();
  }

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
            .then(this.literal("resourcepack").executes(this::sendResourcepack))
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
    final String folder = "%s/emc/".formatted(this.plugin.getDataFolder().getAbsolutePath());

    this.attributes.getCompletion().set(false);

    final Optional<VideoMRLType> type = VideoMRLType.getType(mrl);
    if (type.isEmpty()) {
      red(audience,
          "Invalid MRL link! Does not follow a Spotify/Youtube/Local File/Direct Link pattern!");
      return SINGLE_SUCCESS;
    }

    gold(
        audience,
        "Creating a resourcepack for audio. Depending on the length of the video, it make take some time.");

    switch (type.get()) {
      case LOCAL_FILE -> this.task = CompletableFuture.runAsync(
              () -> this.wrapResourcepack(this.prepareLocalFileVideo(audience, Path.of(mrl), folder)))
          .thenRunAsync(() -> this.afterDownloadExecutionFinish(audience, mrl));
      case YOUTUBE -> this.task = CompletableFuture.runAsync(
              () ->
                  this.wrapResourcepack(
                      this.prepareYoutubeVideo(audience, Path.of(folder), mrl)))
          .thenRunAsync(() -> this.afterDownloadExecutionFinish(audience, mrl));
      case SPOTIFY -> this.task = CompletableFuture.runAsync(
              () ->
                  this.wrapResourcepack(
                      this.prepareSpotifyLink(audience, Path.of(folder), mrl)))
          .thenRunAsync(() -> this.afterDownloadExecutionFinish(audience, mrl));
      case DIRECT_URL -> this.task = CompletableFuture.runAsync(
              () ->
                  this.wrapResourcepack(
                      this.prepareDirectLink(audience, Path.of(folder), mrl)))
          .thenRunAsync(() -> this.afterDownloadExecutionFinish(audience, mrl));
      default -> throw new IllegalArgumentException("Illegal video type!");
    }

    return SINGLE_SUCCESS;
  }

  private void afterDownloadExecutionFinish(@NotNull final Audience audience,
      @NotNull final String mrl) {
    this.sendCompletionMessage(audience, mrl);
    this.attributes.getCompletion().set(true);
    this.cancelled = false;
  }

  private void sendCompletionMessage(@NotNull final Audience audience, @NotNull final String mrl) {
    if (!this.cancelled) {
      ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), Bukkit.getOnlinePlayers(),
          this.attributes.getUrl(), this.attributes.getHash());
      // this.plugin.audience().players().sendMessage(RESOURCEPACK_MESSAGE);
      gold(audience, "Successfully loaded video %s".formatted(mrl));
    }
  }

  private @NotNull Path prepareLocalFileVideo(@NotNull final Audience audience,
      @NotNull final Path file, @NotNull final String folder) {
    try {
      final Path audio = Path.of(folder, "local.ogg");
      this.attributes.setYoutube(false);
      this.attributes.setExtractor(null);
      final FFmpegAudioExtractor extractor = new FFmpegAudioExtractor(
          this.plugin.library(), this.plugin.getAudioConfiguration(), file, audio);
      this.attributes.setExtractor(extractor);
      extractor.executeAsyncWithLogging((line) -> external(audience, line)).get();
      this.attributes.setVideoMrl(file.toAbsolutePath().toString());
      this.attributes.setAudio(audio);
      return audio;
    } catch (final IOException | ExecutionException | InterruptedException e) {
      this.plugin.getLogger().severe("Failed to extract audio file!");
      e.printStackTrace();
      throw new AssertionError("Unable to create audio from local video!");
    }
  }

  private @NotNull Path prepareYoutubeVideo(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final String mrl) {
    try {
      this.attributes.setYoutube(true);
      final YoutubeVideoAudioExtractor extractor =
          new YoutubeVideoAudioExtractor(
              this.plugin.library(),
              this.plugin.getAudioConfiguration(),
              mrl,
              folder.resolve("yt.ogg"));
      this.attributes.setExtractor(extractor);
      extractor.executeAsyncWithLogging((line) -> external(audience, line)).get();
      this.attributes.setVideoMrl(
          extractor.getDownloader().getDownloadPath().toAbsolutePath().toString());
      this.attributes.setAudio(extractor.getExtractor().getOutput().toAbsolutePath());
      return extractor.getExtractor().getOutput();
    } catch (final ExecutionException | InterruptedException | IOException e) {
      this.plugin.getLogger().severe("Failed to extract audio file!");
      e.printStackTrace();
      throw new AssertionError("Unable to create audio from Youtube video!");
    }
  }

  private @NotNull Path prepareSpotifyLink(@NotNull final Audience audience,
      @NotNull final Path folder, @NotNull final String mrl) {
    try {
      this.attributes.setExtractor(null);
      this.attributes.setYoutube(false);
      final Path audio = folder.resolve("spotify.ogg");
      final SpotifyTrackExtractor downloader = new SpotifyTrackExtractor(this.plugin.library(),
          this.plugin.getAudioConfiguration(), mrl,
          audio);
      this.attributes.setExtractor(downloader);
      downloader.executeAsyncWithLogging((line) -> external(audience, line)).get();
      this.attributes.setVideoMrl(downloader.getTrackDownloader().getDownloadPath().toString());
      this.attributes.setAudio(downloader.getYoutubeExtractor().getExtractor().getOutput());
      return audio;
    } catch (final ExecutionException | InterruptedException | IOException e) {
      this.plugin.getLogger().severe("Failed to extract audio file!");
      e.printStackTrace();
      throw new AssertionError("Unable to create audio from Spotify video!");
    }
  }

  private @NotNull Path prepareDirectLink(@NotNull final Audience audience,
      @NotNull final Path folder, @NotNull final String mrl) {
    try {
      gold(audience, "Downloading video from direct link...");
      final Path video = DependencyUtils.downloadFile(folder.resolve("temp.mp4"), mrl);
      gold(audience, "Finished downloading video from direct link!");
      return this.prepareLocalFileVideo(audience, video, folder.toString());
    } catch (final IOException e) {
      this.plugin.getLogger().severe("Failed to extract audio file!");
      e.printStackTrace();
      throw new AssertionError("Unable to create audio from direct link!");
    }
  }

  private void wrapResourcepack(@NotNull final Path audio) {
    try {
      final HttpServer daemon = this.plugin.getHttpServer();
      final ResourcepackSoundWrapper wrapper =
          new ResourcepackSoundWrapper(
              daemon.getDaemon().getServerPath().resolve("resourcepack.zip"),
              "Youtube Audio",
              PackFormat.getCurrentFormat().getId());
      wrapper.addSound(this.plugin.getName().toLowerCase(Locale.ROOT), audio);
      wrapper.wrap();
      final Path path = wrapper.getResourcepackFilePath();
      this.attributes.setUrl(daemon.createUrl(path));
      this.attributes.setHash(HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));
    } catch (final IOException e) {
      this.plugin.getLogger().severe("Failed to wrap resourcepack!");
      e.printStackTrace();
    }
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Audience audience = this.plugin.audience().sender(sender);
    if (this.unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }
    if (this.isPlayer(audience, sender)) {
      return SINGLE_SUCCESS;
    }
    final String url = this.attributes.getUrl();
    final byte[] hash = this.attributes.getHash();
    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(),
        Collections.singleton((Player) sender), url, hash);
    gold(audience, "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash)));
    return SINGLE_SUCCESS;
  }

  private boolean isPlayer(@NotNull final Audience audience, @NotNull final CommandSender sender) {
    if (!(sender instanceof Player)) {
      red(audience, "You must be a player to execute this command!");
      return true;
    }
    return false;
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    if (this.attributes.getUrl() == null && this.attributes.getHash() == null) {
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
