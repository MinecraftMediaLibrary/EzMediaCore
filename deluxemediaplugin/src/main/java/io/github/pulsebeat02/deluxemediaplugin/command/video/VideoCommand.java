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
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNonNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static java.util.Map.entry;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.PlaybackType;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegAudioTrimmer;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.utility.concurrency.ThreadUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
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

  public VideoCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "video", executor, "deluxemediaplugin.command.video", "");
    this.attributes = plugin.getAttributes();
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(this.literal("play").executes(this::playVideo))
            .then(this.literal("stop").executes(this::stopVideo))
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
    this.createVideoPlayer(sender, players);
    this.sendPlayInformation(audience);
    this.setProperAudioHandler();
    this.attributes.cancelCurrentStream();
    this.handleStreamPlayers(audience);
    this.attributes.getPlayer().start(this.attributes.getVideoMrl());

    return SINGLE_SUCCESS;
  }

  private void createVideoPlayer(
      @NotNull final CommandSender sender, @NotNull final Collection<? extends Player> players) {
    final PlaybackType type = this.attributes.getVideoType();
    type.getHandle().createVideoPlayer(this.plugin(), this.attributes, sender, players);
  }

  private void handleStreamPlayers(@NotNull final Audience audience) {
    this.attributes
        .getAudioOutputType()
        .getHandle()
        .setAudioHandler(
            this.plugin(), this.attributes, audience, this.attributes.getVideoMrl().getMrl());
  }

  private void setProperAudioHandler() {
    this.attributes
        .getAudioOutputType()
        .getHandle()
        .setProperAudioHandler(this.plugin(), this.attributes);
  }

  private int stopVideo(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.audience().sender(context.getSource());
    if (this.mediaNotSpecified(audience)
        || this.mediaProcessingIncomplete(audience)
        || this.mediaUninitialized(audience)) {
      return SINGLE_SUCCESS;
    }

    final MediaBot bot = this.plugin().getMediaBot();
    Nill.ifNot(bot, () -> bot.getMusicManager().pauseTrack());

    this.attributes.cancelCurrentStream();

    final VideoPlayer player = this.attributes.getPlayer();
    if (player != null) {
      this.attributes.getPlayer().pause();
    }

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

    CompletableFuture.runAsync(() -> this.buildResourcepack(audience), RESOURCE_WRAPPER_EXECUTOR)
        .thenRun(() -> this.forceResumeLoadResourcepack(audience))
        .handle(Throwing.THROWING_FUTURE);

    return SINGLE_SUCCESS;
  }

  private void forceResumeLoadResourcepack(@NotNull final Audience audience) {

    final String url = this.attributes.getPackUrl();
    final byte[] hash = this.attributes.getPackHash();

    ResourcepackUtils.forceResourcepackLoad(this.plugin().library(), url, hash);

    audience.sendMessage(Locale.RESUME_AUDIO.build());
  }

  private void buildResourcepack(@NotNull final Audience audience) {

    final Audience console = this.plugin().getConsoleAudience();

    try {

      final String oggMrl = this.attributes.getOggMrl().getMrl();
      final Path audio = Path.of(oggMrl);
      final Path ogg = audio.getParent().resolve("trimmed.ogg");
      final long ms = this.attributes.getPlayer().getElapsedMilliseconds();

      console.sendMessage(Locale.RESUMING_VIDEO_MS.build(ms));

      this.executeFFmpegTrimmer(audience, audio, ogg, ms);
      this.loadResumeResourcepack(this.wrapResumeResourcepack(ogg), audio, ogg);

    } catch (final IOException e) {
      console.sendMessage(Locale.ERR_RESOURCEPACK_WRAP.build());
      throw new AssertionError(e);
    }
  }

  private @NotNull ResourcepackSoundWrapper wrapResumeResourcepack(@NotNull final Path ogg)
      throws IOException {

    final DeluxeMediaPlugin plugin = this.plugin();
    final Path target =
        plugin.getHttpServer().getDaemon().getServerPath().resolve("resourcepack.zip");
    final String sound = plugin.getBootstrap().getName().toLowerCase(java.util.Locale.ROOT);

    final ResourcepackSoundWrapper wrapper =
        ResourcepackSoundWrapper.ofSoundPack(target, "Video Pack", 6);
    wrapper.addSound(sound, ogg);
    wrapper.wrap();

    return wrapper;
  }

  private void loadResumeResourcepack(
      @NotNull final ResourcepackSoundWrapper wrapper,
      @NotNull final Path audio,
      @NotNull final Path ogg)
      throws IOException {

    final Path path = wrapper.getResourcepackFilePath();
    final String url = this.plugin().getHttpServer().createUrl(path);
    final byte[] hash = HashingUtils.createHashSha1(path);

    this.attributes.setPackUrl(url);
    this.attributes.setPackHash(hash);

    Files.delete(audio);
    Files.move(ogg, ogg.resolveSibling("audio.ogg"));
  }

  private void executeFFmpegTrimmer(
      @NotNull final Audience audience,
      @NotNull final Path audio,
      @NotNull final Path ogg,
      final long delay) {
    final FFmpegAudioTrimmer trimmer =
        FFmpegAudioTrimmer.ofFFmpegAudioTrimmer(this.plugin().library(), audio, ogg, delay);
    trimmer.executeAsyncWithLogging(
        (line) -> audience.sendMessage(Locale.EXTERNAL_PROCESS.build(line)));
  }

  private boolean mediaNotSpecified(@NotNull final Audience audience) {
    return handleNull(audience, Locale.ERR_VIDEO_NOT_LOADED.build(), this.attributes.getVideoMrl());
  }

  private boolean mediaProcessingIncomplete(@NotNull final Audience audience) {
    final AtomicBoolean atomicBoolean = this.attributes.getCompletion();
    return atomicBoolean != null
        && handleFalse(audience, Locale.ERR_VIDEO_PROCESSING.build(), atomicBoolean.get());
  }

  private void releaseIfPlaying() {
    final VideoPlayer player = this.attributes.getPlayer();
    Nill.ifNot(player, () -> this.releasePlayer(player));
    this.attributes.setPlayer(null);
  }

  private void releasePlayer(@NotNull final VideoPlayer player) {
    final PlayerControls state = player.getPlayerState();
    if (state != PlayerControls.RELEASE) {
      if (state == PlayerControls.START || state == PlayerControls.RESUME) {
        player.pause();
      }
      player.release();
    }
  }

  private boolean mediaUninitialized(@NotNull final Audience audience) {
    return handleNull(audience, Locale.ERR_VIDEO_NOT_LOADED.build(), this.attributes.getPlayer());
  }

  private void sendPlayInformation(@NotNull final Audience audience) {
    final MrlConfiguration mrl = this.attributes.getVideoMrl();
    handleNonNull(audience, Locale.STARTING_VIDEO.build(mrl.getMrl()), mrl);
  }

  @Override
  public @NotNull TextComponent usage() {
    return Locale.getCommandUsageComponent(
        Map.ofEntries(
            entry("/video", "Lists the current video playing"),
            entry("/video play", "Plays the video"),
            entry("/video stop", "Stops the video"),
            entry("/video load [mrl]", "Loads a Youtube link"),
            entry("/video load cancel-download", "Cancels the Youtube download"),
            entry(
                "/video load resourcepack [target selector]",
                "Loads the past resourcepack made for the video for the selected entities"),
            entry(
                "/video set screen-dimension [width:height]", "Sets the resolution of the screen"),
            entry(
                "/video set itemframe-dimension [width:height]",
                "Sets the proper itemframe dimension of the screen"),
            entry("/video set dither [algorithm]", "Sets the specific algorithm for dithering"),
            entry(
                "/video set starting-map [id]",
                "Sets the starting map id from id to id to the Length * Width. (For example 0 - 24 for 5x5 display if you put 0)"),
            entry("/video set mode [mode]", "Sets the video mode"),
            entry("/video set audio-output [output-type]", "Sets the preferable output type")));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
