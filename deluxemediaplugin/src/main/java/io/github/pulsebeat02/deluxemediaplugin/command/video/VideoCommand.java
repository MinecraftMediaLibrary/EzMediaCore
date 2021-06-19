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

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegAudioTrimmerHelper;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.VideoPlayerContext;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final VideoBuilder builder;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    attributes = new VideoCommandAttributes();
    builder = new VideoBuilder(plugin.library(), attributes);
    node =
        literal(getName())
            .requires(super::testPermission)
            .then(literal("play").executes(this::playVideo))
            .then(literal("stop").executes(this::stopVideo))
            .then(literal("resume").executes(this::resumeVideo))
            .then(new VideoLoadCommand(plugin, attributes).node())
            .then(new VideoSettingCommand(plugin, attributes).node())
            .build();
  }

  private int playVideo(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final DeluxeMediaPlugin plugin = plugin();
    final Audience audience = plugin.audience().sender(sender);
    if (mediaNotSpecified(audience) || mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }
    stopIfPlaying();
    final VideoType type = attributes.getVideoType();
    switch (type) {
      case ITEMFRAME:
        attributes.setPlayer(builder.createMapPlayer());
        break;
      case ARMOR_STAND:
        if (sender instanceof Player) {
          attributes.setPlayer(builder.createEntityPlayer((Player) sender));
        } else {
          audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
          return SINGLE_SUCCESS;
        }
        break;
      case CHATBOX:
        attributes.setPlayer(builder.createChatBoxPlayer());
        break;
      case SCOREBOARD:
        attributes.setPlayer(builder.createScoreboardPlayer());
        break;
      case DEBUG_HIGHLIGHTS:
        if (sender instanceof Player) {
          attributes.setPlayer(builder.createBlockHighlightPlayer((Player) sender));
        } else {
          audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
          return SINGLE_SUCCESS;
        }
        break;
    }
    sendPlayInformation(audience);
    attributes.getPlayer().start(Bukkit.getOnlinePlayers());
    return SINGLE_SUCCESS;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    attributes.getPlayer().stop(Bukkit.getOnlinePlayers());
    plugin()
        .audience()
        .sender(context.getSource())
        .sendMessage(format(text("Stopped the Video!", GOLD)));
    return SINGLE_SUCCESS;
  }

  private int resumeVideo(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final DeluxeMediaPlugin plugin = plugin();
    final Audience audience = plugin.audience().sender(sender);
    if (mediaNotSpecified(audience) || mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }
    audience.sendMessage(
        format(
            text(
                "Setting up resourcepack for resuming... this may take a while depending on how large the audio file is.",
                GOLD)));
    CompletableFuture.runAsync(
            () -> {
              final Path audio = attributes.getAudio();
              final Path temp = attributes.getAudio().getParent().resolve("temp.ogg");
              new FFmpegAudioTrimmerHelper(audio, temp, attributes.getPlayer().getElapsedTime())
                  .trim();
              final PackWrapper wrapper = ResourcepackWrapper.of(plugin.library(), temp);
              wrapper.buildResourcePack();
              final Path path = Paths.get(wrapper.getPath());
              attributes.setResourcepackUrl(
                  plugin.getHttpConfiguration().getDaemon().generateUrl(path));
              attributes.setHash(VideoExtractionUtilities.createHashSHA(path));
            })
        .thenRun(this::sendResourcepackFile)
        .thenRunAsync(
            () -> audience.sendMessage(format(text(("Successfully resumed video %s"), GOLD))));
    return SINGLE_SUCCESS;
  }

  public void sendResourcepackFile() {
    final String url = attributes.getResourcepackUrl();
    final byte[] hash = attributes.getHash();
    Bukkit.getOnlinePlayers().forEach(p -> p.setResourcePack(url, hash));
  }

  private boolean mediaNotSpecified(@NotNull final Audience audience) {
    if (attributes.getVideo() == null && !attributes.isYoutube()) {
      audience.sendMessage(format(text("File and URL not specified yet!", RED)));
      return true;
    }
    return false;
  }

  private boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    if (!attributes.getCompletion().get()) {
      audience.sendMessage(format(text("The video is still being processed!", RED)));
      return true;
    }
    return false;
  }

  private void stopIfPlaying() {
    final VideoPlayerContext player = attributes.getPlayer();
    if (player != null && player.isPlaying()) {
      player.stop(Bukkit.getOnlinePlayers());
    }
  }

  private void sendPlayInformation(@NotNull final Audience audience) {
    if (attributes.isYoutube()) {
      audience.sendMessage(
          format(
              text(
                  String.format("Starting Video on URL: %s", attributes.getExtractor().getUrl()),
                  GOLD)));
    } else {
      audience.sendMessage(
          format(
              text(
                  String.format(
                      "Starting Video on File: %s", PathUtilities.getName(attributes.getVideo())),
                  GOLD)));
    }
  }

  @Override
  public TextComponent usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.<String, String>builder()
            .put("/video", "Lists the current video playing")
            .put("/video play", "Plays the video")
            .put("/video stop", "Stops the video")
            .put("/video load [url]", "Loads a Youtube link")
            .put("/video load [file]", "Loads a specific video file")
            .put("/video load resourcepack", "Loads the past resourcepack used for the video")
            .put("/video set screen-dimension [width:height]", "Sets the resolution of the screen")
            .put(
                "/video set itemframe-dimension [width:height]",
                "Sets the proper itemframe dimension of the screen")
            .put("/video set dither [algorithm]", "Sets the specific algorithm for dithering")
            .put(
                "/video set starting-map [id]",
                "Sets the starting map id from id to id + 25. (For example 0 - 24)")
            .put(
                "/video set mode [mode]",
                "Sets whether the video should be entity clouds or itemframes")
            .build());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
