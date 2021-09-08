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
import static java.util.Map.entry;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioTrimmer;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final VideoCreator builder;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    this.attributes = new VideoCommandAttributes();
    this.builder = new VideoCreator(plugin.library(), this.attributes);
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(this.literal("play").executes(this::playVideo))
            .then(this.literal("stop").executes(this::stopVideo))
            .then(this.literal("resume").executes(this::resumeVideo))
            .then(this.literal("destroy").executes(this::destroyVideo))
            .then(new VideoLoadCommand(plugin, this.attributes).node())
            .then(new VideoSettingCommand(plugin, this.attributes).node())
            .build();
  }

  private int destroyVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin().audience().sender(context.getSource());
    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    this.releaseIfPlaying();
    gold(audience, "Successfully destroyed the current video!");

    return SINGLE_SUCCESS;
  }

  private int playVideo(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final DeluxeMediaPlugin plugin = this.plugin();
    final Audience audience = plugin.audience().sender(sender);
    final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }

    this.releaseIfPlaying();

    final VideoType type = this.attributes.getVideoType();
    switch (type) {
      case ITEMFRAME -> this.attributes.setPlayer(this.builder.createMapPlayer(players));
      case ARMOR_STAND -> {
        if (sender instanceof Player) {
          this.attributes.setPlayer(this.builder.createEntityPlayer((Player) sender, players));
        } else {
          audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
          return SINGLE_SUCCESS;
        }
      }
      case CHATBOX -> this.attributes.setPlayer(this.builder.createChatBoxPlayer(players));
      case SCOREBOARD -> this.attributes.setPlayer(this.builder.createScoreboardPlayer(players));
      case DEBUG_HIGHLIGHTS -> {
        if (sender instanceof Player) {
          this.attributes.setPlayer(
              this.builder.createBlockHighlightPlayer((Player) sender, players));
        } else {
          audience.sendMessage(format(text("You must be a player to execute this command!", RED)));
          return SINGLE_SUCCESS;
        }
      }
      default -> throw new IllegalArgumentException("Illegal video mode!");
    }
    this.sendPlayInformation(audience);
    this.attributes.getPlayer().setPlayerState(PlayerControls.START);

    return SINGLE_SUCCESS;
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.audience().sender(context.getSource());
    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }
    this.attributes.getPlayer().setPlayerState(PlayerControls.PAUSE);
    gold(audience, "Stopped the video!");

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

    CompletableFuture.runAsync(() -> this.buildResourcepack(audience))
        .thenRunAsync(
            () ->
                ResourcepackUtils.forceResourcepackLoad(
                    plugin.library(), this.attributes.getUrl(), this.attributes.getHash()))
        .thenRun(() -> gold(audience, "Resumed the video!"));
    return SINGLE_SUCCESS;
  }

  private void buildResourcepack(@NotNull final Audience audience) {

    final DeluxeMediaPlugin plugin = this.plugin();

    try {

      final HttpServer server = plugin.getHttpServer();
      final Path audio = this.attributes.getAudio();
      final Path ogg = audio.getParent().resolve("trimmed.ogg");
      final long ms = this.attributes.getPlayer().getElapsedMilliseconds();

      plugin.log("Resuming Video at %s Milliseconds!".formatted(ms));

      new FFmpegAudioTrimmer(
          plugin.library(), audio, ogg, ms)
          .executeAsyncWithLogging((line) -> external(audience, line));

      final ResourcepackSoundWrapper wrapper =
          new ResourcepackSoundWrapper(
              server.getDaemon().getServerPath().resolve("resourcepack.zip"), "Video Pack", 6);
      wrapper.addSound(plugin.getName().toLowerCase(Locale.ROOT), ogg);
      wrapper.wrap();

      final Path path = wrapper.getResourcepackFilePath();
      this.attributes.setUrl(server.createUrl(path));
      this.attributes.setHash(HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));

      Files.delete(audio);
      Files.move(ogg, ogg.resolveSibling("audio.ogg"));

    } catch (final IOException e) {
      plugin.getLogger().severe("Failed to wrap resourcepack!");
      e.printStackTrace();
    }
  }

  private boolean mediaNotSpecified(@NotNull final Audience audience) {
    if (this.attributes.getVideoMrl() == null && !this.attributes.isYoutube()) {
      red(audience, "File and URL not specified yet!");
      return true;
    }
    return false;
  }

  private boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    if (!this.attributes.getCompletion().get()) {
      red(audience, "The video is still being processed!");
      return true;
    }
    return false;
  }

  private void releaseIfPlaying() {
    final VideoPlayer player = this.attributes.getPlayer();
    if (player != null) {
      if (player.getPlayerState() != PlayerControls.RELEASE) {
        player.setPlayerState(PlayerControls.RELEASE);
      }
    }
  }

  private void sendPlayInformation(@NotNull final Audience audience) {
    final String mrl = this.attributes.getVideoMrl();
    if (mrl != null) {
      if (this.attributes.isYoutube()) {
        gold(audience, "Starting Video on URL: %s".formatted(mrl));
      } else {
        gold(audience, "Starting Video on File: %s".formatted(PathUtils.getName(Path.of(mrl))));
      }
    }
  }

  @Override
  public @NotNull TextComponent usage() {
    return ChatUtils.getCommandUsage(
        Map.ofEntries(
            entry("/video", "Lists the current video playing"),
            entry("/video play", "Plays the video"),
            entry("/video stop", "Stops the video"),
            entry("/video load [url]", "Loads a Youtube link"),
            entry("/video load [file]", "Loads a specific video file"),
            entry("/video load cancel-download", "Cancels the Youtube download"),
            entry("/video load resourcepack", "Loads the past resourcepack used for the video"),
            entry("/video set screen-dimension [width:height]", "Sets the resolution of the screen"),
            entry("/video set itemframe-dimension [width:height]","Sets the proper itemframe dimension of the screen"),
            entry("/video set dither [algorithm]", "Sets the specific algorithm for dithering"),
            entry("/video set starting-map [id]", "Sets the starting map id from id to id to the Length * Width. (For example 0 - 24 for 5x5 display if you put 0)"),
            entry("/video set mode [mode]", "Sets the video mode")));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
