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
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegAudioExtractionHelper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class VideoLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;

  public VideoLoadCommand(@NotNull final VideoCommandAttributes attributes) {
    this.attributes = attributes;
    node =
        literal("load")
            .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadVideo))
            .then(literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final DeluxeMediaPlugin plugin = attributes.getPlugin();
    final Audience audience = plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folder = String.format("%s/mml/", plugin.getDataFolder().getAbsolutePath());
    final AtomicBoolean completion = attributes.getCompletion();
    if (!VideoExtractionUtilities.getYoutubeID(mrl).isPresent()) {
      final Path file = Paths.get(mrl);
      if (Files.exists(file)) {
        completion.set(false);
        attributes.setYoutube(false);
        attributes.setExtractor(null);
        attributes.setFile(file);
        CompletableFuture.runAsync(
                () -> {
                  final Path audio = Paths.get(folder, "custom.ogg");
                  new FFmpegAudioExtractionHelper(
                          plugin.getEncoderConfiguration().getSettings(), file, audio)
                      .extract();
                  final PackWrapper wrapper = ResourcepackWrapper.of(plugin.getLibrary(), audio);
                  wrapper.buildResourcePack();
                  final Path path = Paths.get(wrapper.getPath());
                  attributes.setResourcepackUrl(
                      plugin.getHttpConfiguration().getDaemon().generateUrl(path));
                  attributes.setHash(VideoExtractionUtilities.createHashSHA(path));
                  completion.set(true);
                })
            .thenRun(this::sendResourcepackFile)
            .thenRunAsync(
                () ->
                    audience.sendMessage(
                        format(text(String.format("Successfully loaded video %s", mrl), GOLD))));
      } else if (mrl.startsWith("http")) {
        audience.sendMessage(
            format(text(String.format("Link %s is not a valid Youtube video link!", mrl), RED)));
      } else {
        audience.sendMessage(
            format(
                text(String.format("File %s cannot be found!", PathUtilities.getName(file)), RED)));
      }
    } else {
      completion.set(false);
      attributes.setYoutube(true);
      CompletableFuture.runAsync(
          () -> {
            final YoutubeExtraction extraction =
                new YoutubeExtraction(
                    mrl, Paths.get(folder), plugin.getEncoderConfiguration().getSettings());
            extraction.extractAudio();
            attributes.setFile(extraction.getVideo());
            attributes.setExtractor(extraction);
            final PackWrapper wrapper = ResourcepackWrapper.of(plugin.getLibrary(), extraction);
            wrapper.buildResourcePack();
            attributes.setResourcepackUrl(
                plugin.getHttpConfiguration().getDaemon().generateUrl(wrapper.getPath()));
            attributes.setHash(
                VideoExtractionUtilities.createHashSHA(Paths.get(wrapper.getPath())));
            sendResourcepackFile();
            audience.sendMessage(
                format(text(String.format("Successfully loaded video %s", mrl), GOLD)));
            completion.set(true);
          });
    }
    return SINGLE_SUCCESS;
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = attributes.getPlugin().audience().sender(context.getSource());
    if (unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }
    sendResourcepackFile();
    audience.sendMessage(
        format(
            text(
                String.format(
                    "Sent Resourcepack URL! (%s with hash %s)",
                    attributes.getResourcepackUrl(), new String(attributes.getHash())),
                GOLD)));
    return SINGLE_SUCCESS;
  }

  public void sendResourcepackFile() {
    final String url = attributes.getResourcepackUrl();
    final byte[] hash = attributes.getHash();
    Bukkit.getOnlinePlayers().forEach(p -> p.setResourcePack(url, hash));
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    if (attributes.getResourcepackUrl() == null && attributes.getHash() == null) {
      audience.sendMessage(text("Please load a resourcepack before executing this command!", RED));
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
