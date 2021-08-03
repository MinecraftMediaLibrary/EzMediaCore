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
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioTrimmer;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegAudioTrimmerHelper;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.VideoPlayerContext;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.gold;
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
    this.attributes = new VideoCommandAttributes();
    this.builder = new VideoBuilder(plugin.library(), this.attributes);
    this.node =
            this.literal(this.getName())
            .requires(super::testPermission)
            .then(this.literal("play").executes(this::playVideo))
            .then(this.literal("stop").executes(this::stopVideo))
            .then(this.literal("resume").executes(this::resumeVideo))
            .then(new VideoLoadCommand(plugin, this.attributes).node())
            .then(new VideoSettingCommand(plugin, this.attributes).node())
            .build();
  }

  private int playVideo(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final DeluxeMediaPlugin plugin = this.plugin();
    final Audience audience = plugin.audience().sender(sender);

    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    this.stopIfPlaying();

    final VideoType type = this.attributes.getVideoType();
    switch (type) {
      case ITEMFRAME:
        this.attributes.setPlayer(this.builder.createMapPlayer());
        break;
      case ARMOR_STAND:
        if (sender instanceof Player) {
          this.attributes.setPlayer(this.builder.createEntityPlayer((Player) sender));
        } else {
          audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
          return SINGLE_SUCCESS;
        }
        break;
      case CHATBOX:
        this.attributes.setPlayer(this.builder.createChatBoxPlayer());
        break;
      case SCOREBOARD:
        this.attributes.setPlayer(this.builder.createScoreboardPlayer());
        break;
      case DEBUG_HIGHLIGHTS:
        if (sender instanceof Player) {
          this.attributes.setPlayer(this.builder.createBlockHighlightPlayer((Player) sender));
        } else {
          audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
          return SINGLE_SUCCESS;
        }
        break;
    }
    this.sendPlayInformation(audience);
    this.attributes.getPlayer().setPlayerState(PlayerControls.START);
    return SINGLE_SUCCESS;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    this.attributes.getPlayer().setPlayerState(PlayerControls.PAUSE);
    gold(this.audience().sender(context.getSource()), "Stopped the video!");
    return SINGLE_SUCCESS;
  }

  private int resumeVideo(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final DeluxeMediaPlugin plugin = this.plugin();
    final Audience audience = plugin.audience().sender(sender);

    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    gold(
        audience,
        "Setting up resourcepack for resuming... this may take a while depending on how large the audio file is.");




    CompletableFuture.runAsync(
            () -> {
              final Path audio = this.attributes.getAudio();
              final Path temp = this.attributes.getAudio().getParent().resolve("temp.ogg");
              new FFmpegAudioTrimmerHelper(audio, temp, this.attributes.getPlayer().getElapsedTime())
                  .trim();
              final PackWrapper wrapper = ResourcepackWrapper.of(plugin.library(), temp);
              wrapper.buildResourcePack();
              final Path path = Paths.get(wrapper.getPath());
              this.attributes.setResourcepackUrl(
                  plugin.getHttpConfiguration().getServer().generateUrl(path));
              this.attributes.setHash(VideoExtractionUtilities.createHashSHA(path));
            })
        .thenRunAsync(this::sendResourcepackFile)
        .thenRunAsync(
            () -> audience.sendMessage(format(text(("Successfully resumed video %s"), GOLD))));
    return SINGLE_SUCCESS;
  }

  private void buildResourcepack() {
    final Path audio = this.attributes.getAudio();
    final Path ogg = audio.getParent().resolve("temp.ogg");
    new FFmpegAudioTrimmer(this.plugin().library(), audio, ogg, this.attributes.getPlayer())
  }

  public void sendResourcepackFile() {
    ResourcepackUtils.forceResourcepackLoad(
            this.plugin(), Bukkit.getOnlinePlayers(), this.attributes.getResourcepackUrl(), this.attributes.getHash());
  }

  private boolean mediaNotSpecified(@NotNull final Audience audience) {
    if (this.attributes.getVideo() == null && !this.attributes.isYoutube()) {
      audience.sendMessage(format(text("File and URL not specified yet!", RED)));
      return true;
    }
    return false;
  }

  private boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    if (!this.attributes.getCompletion().get()) {
      audience.sendMessage(format(text("The video is still being processed!", RED)));
      return true;
    }
    return false;
  }

  private void stopIfPlaying() {
    final VideoPlayerContext player = this.attributes.getPlayer();
    if (player != null && player.isPlaying()) {
      player.stop(Bukkit.getOnlinePlayers());
    }
  }

  private void sendPlayInformation(@NotNull final Audience audience) {
    if (this.attributes.isYoutube()) {
      audience.sendMessage(
          format(
              text(
                  String.format("Starting Video on URL: %s", this.attributes.getExtractor().getUrl()),
                  GOLD)));
    } else {
      audience.sendMessage(
          format(
              text(
                  String.format(
                      "Starting Video on File: %s",
                      PathUtilities.getName(Paths.get(this.attributes.getVideo()))),
                  GOLD)));
    }
  }

  @Override
  public TextComponent usage() {
    return ChatUtils.getCommandUsage(
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
    return this.node;
  }
}
