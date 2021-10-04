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
import static java.util.Map.entry;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.bot.audio.MusicManager;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.config.ServerInfo;
import io.github.pulsebeat02.deluxemediaplugin.executors.ExecutorProvider;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioTrimmer;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegMediaStreamer;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import io.github.pulsebeat02.ezmediacore.utility.ThreadUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class VideoCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final VideoCreator builder;

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    this.attributes = plugin.getAttributes();
    this.builder = new VideoCreator(plugin.library(), this.attributes);
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(this.literal("play").executes(this::playVideo))
            .then(this.literal("stop").executes(this::stopVideo))
            .then(this.literal("resume").executes(this::resumeVideo))
            .then(this.literal("destroy").executes(this::destroyVideo))
            .then(this.literal("dump-threads").executes(this::dumpThreads))
            .then(new VideoLoadCommand(plugin, this.attributes).node())
            .then(new VideoSettingCommand(plugin, this.attributes).node())
            .build();
  }

  private int dumpThreads(@NotNull final CommandContext<CommandSender> context) {
    ThreadUtils.createThreadDump();
    this.plugin().audience().sender(context.getSource()).sendMessage(Locale.DUMP_THREADS.build());
    return SINGLE_SUCCESS;
  }

  private int destroyVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin().audience().sender(context.getSource());
    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }
    this.releaseIfPlaying();
    audience.sendMessage(Locale.RELEASE_VIDEO.build());
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
    final PlaybackType type = this.attributes.getVideoType();
    switch (type) {
      case ITEMFRAME -> this.attributes.setPlayer(this.builder.createMapPlayer(players));
      case ARMOR_STAND -> {
        if (sender instanceof Player) {
          this.attributes.setPlayer(this.builder.createEntityPlayer((Player) sender, players));
        } else {
          audience.sendMessage(Locale.ERR_PLAYER_SENDER.build());
          return SINGLE_SUCCESS;
        }
      }
      case CHATBOX -> this.attributes.setPlayer(this.builder.createChatBoxPlayer(players));
      case SCOREBOARD -> this.attributes.setPlayer(this.builder.createScoreboardPlayer(players));
      case DEBUG_HIGHLIGHTS -> {
        if (sender instanceof Player) {
          this.attributes.setPlayer(
              this.builder.createBlockHighlightPlayer((Player) sender));
        } else {
          audience.sendMessage(Locale.ERR_PLAYER_SENDER.build());
          return SINGLE_SUCCESS;
        }
      }
      default -> throw new IllegalArgumentException("Illegal video mode!");
    }

    this.sendPlayInformation(audience);
    this.setProperAudioHandler();
    this.attributes.cancelCurrentStream();
    this.handleStreamPlayers(audience);

    this.attributes.getPlayer().setPlayerState(PlayerControls.START, this.attributes.getVideoMrl());

    return SINGLE_SUCCESS;
  }

  private void handleStreamPlayers(@NotNull final Audience audience) {
    final DeluxeMediaPlugin plugin = this.plugin();
    final String mrl = this.attributes.getVideoMrl().getMrl();
    switch (this.attributes.getAudioOutputType()) {
      case DISCORD -> {
        CompletableFuture.runAsync(() -> {
          final String link = "%s/stream.m3u8".formatted(this.openFFmpegStream(mrl));
          final MediaBot bot = plugin.getMediaBot();
          final MusicManager manager = bot.getMusicManager();
          manager.destroyTrack();
          manager.joinVoiceChannel();
          try {
            TimeUnit.SECONDS.sleep(3L);
          } catch (final InterruptedException e) { // hack to wait for server start
            e.printStackTrace();
          }
          manager.addTrack(link);
          audience.sendMessage(Locale.DISCORD_AUDIO_STREAM.build());
        });
      }
      case HTTP -> {
        plugin.audience().players()
            .sendMessage(Locale.HTTP_SEND_LINK.build(this.openFFmpegStream(mrl)));
      }
      case RESOURCEPACK -> {
      }
      default -> throw new IllegalArgumentException("Illegal Audio Output Option!");
    }
  }

  private void setProperAudioHandler() {
    final VideoPlayer player = this.attributes.getPlayer();
    switch (this.attributes.getAudioOutputType()) {
      case RESOURCEPACK -> player.setCustomAudioPlayback((mrl) -> {
        final Set<Player> viewers = player.getWatchers().getPlayers();
        final String sound = player.getSoundKey().getName();
        for (final Player p : viewers) {
          p.playSound(p.getLocation(), sound, SoundCategory.MASTER, 100.0F, 1.0F);
        }
      });
      case DISCORD, HTTP -> player.setCustomAudioPlayback((mrl) -> {
      });
      default -> throw new IllegalArgumentException("Illegal Audio Output!");
    }
  }

  private String openFFmpegStream(@NotNull final String mrl) {
    final DeluxeMediaPlugin plugin = this.plugin();
    final ServerInfo info = plugin.getHttpAudioServer();
    final String ip = info.getIp();
    final int port = info.getPort();
    final FFmpegMediaStreamer streamer = new FFmpegMediaStreamer(
        plugin.library(), plugin.getAudioConfiguration(), RequestUtils.getAudioURLs(mrl).get(0), ip,
        port);
    this.attributes.setStreamExtractor(streamer);
    streamer.executeAsync(ExecutorProvider.STREAM_THREAD_EXECUTOR);
    return "http://%s:%s/live.stream".formatted(ip, port);
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.audience().sender(context.getSource());
    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)
        || this.mediaUninitialized(audience)) {
      return SINGLE_SUCCESS;
    }
    final MediaBot bot = this.plugin().getMediaBot();
    if (bot != null) {
      bot.getMusicManager().pauseTrack();
    }
    this.attributes.cancelCurrentStream();
    this.attributes.getPlayer().setPlayerState(PlayerControls.PAUSE);
    audience.sendMessage(Locale.PAUSE_VIDEO.build());
    return SINGLE_SUCCESS;
  }

  private int resumeVideo(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final DeluxeMediaPlugin plugin = this.plugin();
    final Audience audience = plugin.audience().sender(sender);
    if (this.mediaNotSpecified(audience) || this.mediaProcessingIncomplete(audience)) {
      return SINGLE_SUCCESS;
    }
    audience.sendMessage(Locale.SETUP_RESOURCEPACK.build());
    CompletableFuture.runAsync(() -> this.buildResourcepack(audience))
        .thenRunAsync(
            () ->
                ResourcepackUtils.forceResourcepackLoad(
                    plugin.library(), this.attributes.getResourcepackUrl(),
                    this.attributes.getResourcepackHash()))
        .thenRun(() -> audience.sendMessage(Locale.RESUME_AUDIO.build()));
    return SINGLE_SUCCESS;
  }

  private void buildResourcepack(@NotNull final Audience audience) {
    final DeluxeMediaPlugin plugin = this.plugin();
    final Audience console = plugin.getConsoleAudience();
    final JavaPlugin loader = plugin.getBootstrap();
    try {
      final HttpServer server = plugin.getHttpServer();
      final Path audio = Path.of(this.attributes.getOggMrl().getMrl());
      final Path ogg = audio.getParent().resolve("trimmed.ogg");
      final long ms = this.attributes.getPlayer().getElapsedMilliseconds();
      console.sendMessage(Locale.RESUMING_VIDEO_MS.build(ms));
      new FFmpegAudioTrimmer(
          plugin.library(), audio, ogg, ms)
          .executeAsyncWithLogging(
              (line) -> audience.sendMessage(Locale.EXTERNAL_PROCESS.build(line)));
      final ResourcepackSoundWrapper wrapper =
          new ResourcepackSoundWrapper(
              server.getDaemon().getServerPath().resolve("resourcepack.zip"), "Video Pack", 6);
      wrapper.addSound(loader.getName().toLowerCase(java.util.Locale.ROOT), ogg);
      wrapper.wrap();
      final Path path = wrapper.getResourcepackFilePath();
      this.attributes.setResourcepackUrl(server.createUrl(path));
      this.attributes.setResourcepackHash(
          HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));
      Files.delete(audio);
      Files.move(ogg, ogg.resolveSibling("audio.ogg"));
    } catch (final IOException e) {
      console.sendMessage(Locale.ERR_RESOURCEPACK_WRAP.build());
      e.printStackTrace();
    }
  }

  private boolean mediaNotSpecified(@NotNull final Audience audience) {
    if (this.attributes.getVideoMrl() == null) {
      audience.sendMessage(Locale.ERR_VIDEO_NOT_LOADED.build());
      return true;
    }
    return false;
  }

  private boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    if (!this.attributes.getCompletion().get()) {
      audience.sendMessage(Locale.ERR_VIDEO_PROCESSING.build());
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

  private boolean mediaUninitialized(@NotNull final Audience audience) {
    if (this.attributes.getPlayer() == null) {
      audience.sendMessage(Locale.ERR_VIDEO_NOT_LOADED.build());
      return true;
    }
    return false;
  }

  private void sendPlayInformation(@NotNull final Audience audience) {
    final MrlConfiguration mrl = this.attributes.getVideoMrl();
    if (mrl != null) {
      audience.sendMessage(Locale.STARTING_VIDEO.build(mrl.getMrl()));
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
            entry("/video load resourcepack [target selector]",
                "Loads the past resourcepack made for the video"),
            entry("/video set screen-dimension [width:height]",
                "Sets the resolution of the screen"),
            entry("/video set itemframe-dimension [width:height]",
                "Sets the proper itemframe dimension of the screen"),
            entry("/video set dither [algorithm]", "Sets the specific algorithm for dithering"),
            entry("/video set starting-map [id]",
                "Sets the starting map id from id to id to the Length * Width. (For example 0 - 24 for 5x5 display if you put 0)"),
            entry("/video set mode [mode]", "Sets the video mode"),
            entry("/video set audio-output [output-type]", "Sets the preferable output type")));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
