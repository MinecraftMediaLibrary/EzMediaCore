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

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.YoutubeVideoDownloader;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.error;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.info;

public final class AudioLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public AudioLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final AudioCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    node =
        literal("load")
            .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadAudio))
            .then(literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private int loadAudio(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = plugin.audience().sender(context.getSource());
    final MediaLibraryCore library = plugin.library();
    final String mrl = context.getArgument("mrl", String.class);

    if (isLoadingSound(audience)) {
      return SINGLE_SUCCESS;
    }

    info(
        audience,
        "Attempting to load the audio file... this may take a while depending on the length/quality of the audio.");

    if (isUrl(mrl)) {

      CompletableFuture.runAsync(
          () -> {
            final Path folder = library.getAudioPath();
            final Path video = folder.resolve("video.mp4");
            final Path audio = folder.resolve("audio.ogg");

            final YoutubeVideoDownloader downloader = new YoutubeVideoDownloader(mrl, video);
            downloader.downloadVideo(
                downloader.getVideo().getVideoFormats().get(0).getQuality(), true);

            final FFmpegAudioExtractor extractor =
                new FFmpegAudioExtractor(
                    library, plugin.getEncoderConfiguration().getSettings(), video, audio);
            extractor.execute();

            attributes.setResourcepackAudio(audio);
          });
    } else {

      final Path file = Paths.get(mrl);
      if (Files.exists(file)) {
        attributes.setResourcepackAudio(file);
      } else {
        error(audience, "The mrl specified is not valid! (Must be Youtube Link or Audio File)");
        return SINGLE_SUCCESS;
      }
    }

    CompletableFuture.runAsync(
            () -> {
              attributes.setCompletion(true);

              try {
                final ResourcepackSoundWrapper wrapper =
                    new ResourcepackSoundWrapper(
                        library.getHttpServerPath().resolve("pack.zip"), "", 6);
                wrapper.addSound(
                    plugin.getName().toLowerCase(Locale.ROOT), attributes.getResourcepackAudio());
                wrapper.wrap();

                final Path path = wrapper.getResourcepackFilePath();

                attributes.setResourcepackLink(
                    plugin.getHttpConfiguration().getServer().createUrl(path));

                attributes.setResourcepackHash(
                    HashingUtils.getHash(path).getBytes(StandardCharsets.UTF_8));

              } catch (IOException e) {
                e.printStackTrace();
              }

              sendResourcepack();

              info(
                  audience,
                  String.format(
                      "Loaded Audio Successfully! (%s)", attributes.getResourcepackLink()));
            })
        .whenCompleteAsync((t, throwable) -> attributes.setCompletion(false));
    return SINGLE_SUCCESS;
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    if (unloadedResourcepack()) {
      error(audience, "Please load a resourcepack first!");
      return SINGLE_SUCCESS;
    }
    sendResourcepack();
    info(
        audience,
        String.format(
            "Sent Resourcepack URL! (%s with hash %s)",
            attributes.getResourcepackLink(), new String(attributes.getResourcepackHash())));
    return SINGLE_SUCCESS;
  }

  private void sendResourcepack() {
    ResourcepackUtils.forceResourcepackLoad(
        plugin,
        Bukkit.getOnlinePlayers(),
        attributes.getResourcepackLink(),
        attributes.getResourcepackHash());
  }

  private boolean isUrl(@NotNull final String url) {
    return url.startsWith("http");
  }

  private boolean unloadedResourcepack() {
    return attributes.getResourcepackLink() == null && attributes.getResourcepackHash() == null;
  }

  private boolean isLoadingSound(@NotNull final Audience audience) {
    if (attributes.getCompletion().get()) {
      error(
          audience,
          "Please wait for the previous audio to extract first before loading another one!");
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
