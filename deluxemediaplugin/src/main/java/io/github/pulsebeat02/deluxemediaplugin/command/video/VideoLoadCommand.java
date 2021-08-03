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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.YoutubeVideoDownloader;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.gold;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class VideoLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;
  private boolean firstLoad;

  public VideoLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.firstLoad = true;
    this.node =
        this.literal("load")
            .then(this.argument("mrl", StringArgumentType.greedyString()).executes(this::loadVideo))
            .then(this.literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folder = String.format("%s/emc/", this.plugin.getDataFolder().getAbsolutePath());
    final AtomicBoolean completion = this.attributes.getCompletion();
    audience.sendMessage(
        format(
            text(
                "Setting up resourcepack for video... this may take a while depending on the length/quality of the video.",
                GOLD)));
    if (!MediaExtractionUtils.getYoutubeID(mrl).isPresent()) {
      final Path file = Paths.get(mrl);
      if (Files.exists(file)) {
        CompletableFuture.runAsync(() -> completion.set(false))
            .thenRunAsync(
                () -> this.wrapResourcepack(this.setAudioFileAttributes(Paths.get(mrl), folder)))
            .thenRun(this::sendResourcepackFile)
            .thenRun(this::useFirstLoad)
            .thenRun(() -> this.sendSuccessfulLoadMessage(audience, mrl))
            .thenRun(() -> completion.set(true));
      } else if (mrl.startsWith("http")) {
        red(audience, String.format("Link %s is not a valid Youtube video link!", mrl));
      } else {
        red(audience, String.format("File %s cannot be found!", PathUtils.getName(file)));
      }
    } else {
      CompletableFuture.runAsync(() -> completion.set(false))
          .thenRunAsync(
              () -> this.wrapResourcepack(this.setYoutubeAttributes(Paths.get(folder), mrl)))
          .thenRun(this::sendResourcepackFile)
          .thenRun(this::useFirstLoad)
          .thenRun(() -> this.sendSuccessfulLoadMessage(audience, mrl))
          .thenRun(() -> completion.set(true));
    }
    return SINGLE_SUCCESS;
  }

  private void useFirstLoad() {
    if (this.firstLoad) {
      this.sendResourcepackFile();
      this.firstLoad = false;
    }
  }

  private Path setAudioFileAttributes(@NotNull final Path file, @NotNull final String folder) {

    final Path audio = Paths.get(folder, "custom.ogg");
    new FFmpegAudioExtractor(
            this.plugin.library(), this.plugin.getEncoderConfiguration().getSettings(), file, audio)
        .execute();

    this.attributes.setYoutube(false);
    this.attributes.setVideoMrl(file.toString());
    this.attributes.setAudio(audio);

    return audio;
  }

  private FFmpegAudioExtractor setYoutubeAttributes(
      @NotNull final Path folder, @NotNull final String mrl) {

    final YoutubeVideoDownloader downloader =
        new YoutubeVideoDownloader(mrl, folder.resolve("video.mp4"));
    downloader.downloadVideo(downloader.getVideo().getVideoFormats().get(0).getQuality(), true);

    final FFmpegAudioExtractor extractor =
        new FFmpegAudioExtractor(
            this.plugin.library(),
            this.plugin.getEncoderConfiguration().getSettings(),
            downloader.getDownloadPath(),
            folder.resolve("audio.ogg"));
    extractor.execute();

    this.attributes.setYoutube(true);
    this.attributes.setVideoMrl(mrl);
    this.attributes.setAudio(extractor.getOutput());

    return extractor;
  }

  private void wrapResourcepack(@NotNull final Path audio) {

    try {

      final HttpServer daemon = this.plugin.getHttpConfiguration().getServer();
      final ResourcepackSoundWrapper wrapper =
          new ResourcepackSoundWrapper(daemon.getDaemon().getServerPath(), "Youtube Audio", 6);
      wrapper.addSound(this.plugin.getName().toLowerCase(Locale.ROOT), audio);
      wrapper.wrap();

      final Path path = wrapper.getResourcepackFilePath();

      this.attributes.setResourcepackUrl(daemon.createUrl(path));
      this.attributes.setHash(HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));

    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void wrapResourcepack(@NotNull final FFmpegAudioExtractor extraction) {

    try {

      final HttpServer daemon = this.plugin.getHttpConfiguration().getServer();
      final ResourcepackSoundWrapper wrapper =
          new ResourcepackSoundWrapper(daemon.getDaemon().getServerPath(), "Youtube Audio", 6);
      wrapper.addSound(this.plugin.getName().toLowerCase(Locale.ROOT), extraction.getOutput());
      wrapper.wrap();

      final Path path = wrapper.getResourcepackFilePath();

      this.attributes.setResourcepackUrl(daemon.createUrl(path));
      this.attributes.setHash(HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));

    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void sendSuccessfulLoadMessage(
      @NotNull final Audience audience, @NotNull final String mrl) {
    audience.sendMessage(format(text(String.format("Successfully loaded video %s", mrl), GOLD)));
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }

    this.sendResourcepackFile();

    gold(
        audience,
        String.format(
            "Sent Resourcepack URL! (%s with hash %s)",
            this.attributes.getResourcepackUrl(), new String(this.attributes.getHash())));

    return SINGLE_SUCCESS;
  }

  private void sendResourcepackFile() {
    ResourcepackUtils.forceResourcepackLoad(
        this.plugin,
        Bukkit.getOnlinePlayers(),
        this.attributes.getResourcepackUrl(),
        this.attributes.getHash());
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    if (this.attributes.getResourcepackUrl() == null && this.attributes.getHash() == null) {
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
