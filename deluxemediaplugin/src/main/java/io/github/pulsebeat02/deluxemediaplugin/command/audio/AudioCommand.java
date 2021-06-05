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

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpServerDaemon;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;
  private final AtomicBoolean atomicBoolean;
  private final String key;
  private Path audio;
  private String resourcepackLink;
  private byte[] hash;

  public AudioCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "audio", executor, "deluxemediaplugin.command.audio", "");
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(
            literal("load")
                .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadAudio))
                .then(literal("resourcepack").executes(this::getResourcepackLink)))
        .then(literal("play").executes(this::playAudio))
        .then(literal("stop").executes(this::stopAudio));
    key = getPlugin().getName().toLowerCase();
    literalNode = builder.build();
    atomicBoolean = new AtomicBoolean(false);
  }

  private int getResourcepackLink(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    if (resourcepackLink == null && hash == null) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text(
                  "Please load a resourcepack first before executing this command!",
                  NamedTextColor.RED)));
      return 1;
    }
    sendResourcepackFile();
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format("Sent Resourcepack URL! (%s)", resourcepackLink),
                NamedTextColor.GOLD)));
    return 1;
  }

  private int playAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    if (audio == null) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("File and URL not Specified!", NamedTextColor.RED)));
      return 1;
    }
    if (!atomicBoolean.get()) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("The audio is still being loaded!", NamedTextColor.RED)));
      return 1;
    }

    // Play the sound to all users on the server
    for (final Player p : Bukkit.getOnlinePlayers()) {
      p.playSound(p.getLocation(), key, SoundCategory.MUSIC, 100.0F, 1.0F);
    }

    audience.sendMessage(
        ChatUtilities.formatMessage(Component.text("Started playing audio!", NamedTextColor.GOLD)));

    return 1;
  }

  private int stopAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    if (audio == null) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("File and URL not Specified!", NamedTextColor.RED)));
      return 1;
    }
    if (!atomicBoolean.get()) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("The audio is still being loaded!", NamedTextColor.RED)));
      return 1;
    }

    // Stop the sound to all users on the server
    for (final Player p : Bukkit.getOnlinePlayers()) {
      p.stopSound(key);
    }

    audience.sendMessage(
        ChatUtilities.formatMessage(Component.text("Stopped playing audio!", NamedTextColor.GOLD)));

    return 1;
  }

  private int loadAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final MediaLibrary library = getPlugin().getLibrary();
    final String mrl = context.getArgument("mrl", String.class);
    if (mrl.startsWith("https://")) {

      CompletableFuture.runAsync(
          () -> {

            // Create a new Youtube Extractor from the url
            final YoutubeExtraction extraction =
                new YoutubeExtraction(
                    mrl,
                    Paths.get(library.getAudioFolder().toString()),
                    getPlugin().getEncoderConfiguration().getSettings());

            // Extract the audio
            audio = extraction.extractAudio();
          });

    } else {

      // Create a new file
      final Path file = Paths.get(mrl);

      // Check if the file exists
      if (Files.exists(file)) {

        // Assign it then
        audio = file;

      } else {
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text(
                    "The mrl specified is not valid! It must be a Youtube link or an audio file.",
                    NamedTextColor.RED)));
        return 1;
      }
    }

    // Make the resourcepack wrapping process async
    CompletableFuture.runAsync(
            () -> {

              // Create a resourcepack from the audio and library instance
              final PackWrapper wrapper = ResourcepackWrapper.of(library, audio);

              // Build the resourcepack
              wrapper.buildResourcePack();

              // Send the resourcepack to players
              sendResourcepack(getPlugin().getHttpConfiguration().getDaemon(), audience, wrapper);

              audience.sendMessage(
                  ChatUtilities.formatMessage(
                      Component.text("Loaded Audio!", NamedTextColor.GOLD)));
            })
        .whenCompleteAsync((t, throwable) -> atomicBoolean.set(true));

    return 1;
  }

  private void sendResourcepack(
      @Nullable final HttpServerDaemon provider,
      @NotNull final Audience audience,
      @NotNull final PackWrapper wrapper) {
    if (provider != null) {

      // Generates a url given by the HTTP server for a file
      resourcepackLink = provider.generateUrl(wrapper.getPath());
      hash = VideoExtractionUtilities.createHashSHA(Paths.get(wrapper.getPath()));

      // Send the resourcepack url to all players on the server
      sendResourcepackFile();

    } else {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text(
                  "You have HTTP set false by default. You cannot play audio without a daemon",
                  NamedTextColor.RED)));
    }
  }

  private void sendResourcepackFile() {
    for (final Player p : Bukkit.getOnlinePlayers()) {
      p.setResourcePack(resourcepackLink, hash);
    }
  }

  @Override
  public Component usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.<String, String>builder()
            .put("/audio load [url]", "Loads a Youtube Link")
            .put("/audio load [file]", "Loads a specific audio file")
            .put("/audio load resourcepack", "Loads the past resourcepack used for the audio")
            .put("/audio play", "Plays the audio to players")
            .put("/audio stop", "Stops the audio")
            .build());
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }
}
