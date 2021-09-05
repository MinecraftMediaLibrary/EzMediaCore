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
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.ffmpeg.YoutubeVideoAudioExtractor;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class VideoLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;
  private CompletableFuture<Void> downloadTask;
  private volatile boolean cancelled;
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
            .then(this.literal("cancel-download").executes(this::cancelDownload))
            .build();
  }

  private int cancelDownload(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final YoutubeVideoAudioExtractor extractor = this.attributes.getExtractor();
    if (extractor != null) {
      this.cancelled = true;
      extractor.cancelProcess();
      this.downloadTask.cancel(true);
      this.attributes.setExtractor(null);
      this.downloadTask = null;
      gold(audience, "Successfully cancelled the Youtube download!");
    } else {
      red(audience, "You haven't downloaded a Youtube video yet!");
    }
    return SINGLE_SUCCESS;
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folder = "%s/emc/".formatted(this.plugin.getDataFolder().getAbsolutePath());
    final AtomicBoolean completion = this.attributes.getCompletion();

    gold(
        audience,
        "Creating a resourcepack for audio. Depending on the length of the video, it make take some time.");

    completion.set(false);

    if (MediaExtractionUtils.getYoutubeID(mrl).isEmpty()) {
      final Path file = Path.of(mrl);
      if (Files.exists(file)) {
        CompletableFuture.runAsync(
                () -> this.wrapResourcepack(this.setAudioFileAttributes(Path.of(mrl), folder)))
            .thenRun(this::useFirstLoad)
            .thenRun(() -> gold(audience, "Successfully loaded file %s".formatted(mrl)))
            .whenComplete((result, exception) -> completion.set(true));
      } else if (mrl.startsWith("http")) {
        red(audience, "Link %s is not a valid Youtube video link!".formatted(mrl));
      } else {
        red(audience, "File %s cannot be found!".formatted(PathUtils.getName(file)));
      }
    } else {
      this.downloadTask = CompletableFuture.runAsync(
              () ->
                  this.wrapResourcepack(
                      this.setYoutubeAttributes(audience, Path.of(folder), mrl)
                          .getExtractor()
                          .getOutput()))
          .thenRunAsync(this::useFirstLoad)
          .thenRun(() -> this.sendCompletionMessage(audience, mrl))
          .whenComplete((result, exception) -> {
            completion.set(true);
            this.cancelled = false;
          });
    }

    return SINGLE_SUCCESS;
  }

  private void sendCompletionMessage(@NotNull final Audience audience, @NotNull final String mrl) {
    if (!this.cancelled) {
      gold(audience, "Successfully loaded Youtube video %s".formatted(mrl));
    }
  }

  private void useFirstLoad() {
    if (!this.cancelled) {
      this.loadResourcepack();
      if (this.firstLoad) {
        this.loadResourcepack();
        this.firstLoad = false;
      }
    }
  }

  private void loadResourcepack() {
    ResourcepackUtils.forceResourcepackLoad(
        this.plugin.library(), this.attributes.getUrl(), this.attributes.getHash());
  }

  private @NotNull Path setAudioFileAttributes(
      @NotNull final Path file, @NotNull final String folder) {

    final Path audio = Path.of(folder, "custom.ogg");

    this.attributes.setYoutube(false);
    this.attributes.setExtractor(null);

    try {
      new FFmpegAudioExtractor(
          this.plugin.library(), this.plugin.getAudioConfiguration(), file, audio)
          .execute();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    this.attributes.setVideoMrl(file.toAbsolutePath().toString());
    this.attributes.setAudio(audio);

    return audio;
  }

  private @NotNull YoutubeVideoAudioExtractor setYoutubeAttributes(
      @NotNull final Audience audience, @NotNull final Path folder, @NotNull final String mrl) {

    try {

      final YoutubeVideoAudioExtractor extractor =
          new YoutubeVideoAudioExtractor(
              this.plugin.library(),
              this.plugin.getAudioConfiguration(),
              mrl,
              folder.resolve("audio.ogg"));

      this.attributes.setExtractor(extractor);

      extractor.executeAsyncWithLogging((line) -> external(audience, line)).get();

      this.attributes.setYoutube(true);
      this.attributes.setVideoMrl(
          extractor.getDownloader().getDownloadPath().toAbsolutePath().toString());
      this.attributes.setAudio(extractor.getExtractor().getOutput().toAbsolutePath());

      return extractor;

    } catch (final ExecutionException | InterruptedException | IOException e) {
      throw new AssertionError("Couldn't extract audio file!");
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

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }

    final String url = this.attributes.getUrl();
    final byte[] hash = this.attributes.getHash();

    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), url, hash);

    gold(audience, "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash)));

    return SINGLE_SUCCESS;
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
