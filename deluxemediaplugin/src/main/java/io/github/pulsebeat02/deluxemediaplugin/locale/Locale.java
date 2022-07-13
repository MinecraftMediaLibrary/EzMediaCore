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

package io.github.pulsebeat02.deluxemediaplugin.locale;

import io.github.pulsebeat02.deluxemediaplugin.command.video.output.DitheringAlgorithm;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

public interface Locale {

  //
  NullComponent<Sender> PLUGIN_INFO = () -> text()
      .append(text("-----------------------------------", GOLD))
      .append(newline())
      .append(text("Author: ", GOLD, BOLD))
      .append(
          text(
              "PulseBeat_02",
              style(
                  AQUA,
                  text("Github", GOLD).asHoverEvent(),
                  openUrl("https://github.com/PulseBeat02"))))
      .append(newline())
      .append(text("Version: ", GOLD, BOLD))
      .append(text("v1.0.0", AQUA))
      .append(newline())
      .append(
          text(
              "Discord",
              style(
                  GOLD,
                  BOLD,
                  openUrl("https://discord.gg/AqK5dKdUZe"))))
      .append(newline())
      .append(text("-----------------------------------", GOLD))
      .append()
      .build();
  //
  NullComponent<Sender> LOGO = () -> getComponent(
      """
            _____       _                __  __          _ _       _____  _             _      \s
           |  __ \\     | |              |  \\/  |        | (_)     |  __ \\| |           (_)     \s
           | |  | | ___| |_   ___  _____| \\  / | ___  __| |_  __ _| |__) | |_   _  __ _ _ _ __ \s
           | |  | |/ _ \\ | | | \\ \\/ / _ \\ |\\/| |/ _ \\/ _` | |/ _` |  ___/| | | | |/ _` | | '_ \\\s
           | |__| |  __/ | |_| |>  <  __/ |  | |  __/ (_| | | (_| | |    | | |_| | (_| | | | | |
           |_____/ \\___|_|\\__,_/_/\\_\\___|_|  |_|\\___|\\__,_|_|\\__,_|_|    |_|\\__,_|\\__, |_|_| |_|
                                                                                   __/ |       \s
                                                                                  |___/        \s
          """,
      (line) -> newline().append(text(line, BLUE)));
  NullComponent<Sender> PREFIX = () ->
      text()
          .color(AQUA)
          .append(
              text('['), text("DeluxeMediaPlugin", GOLD), text(']'), space(), text("»", GRAY))
          .build();
  NullComponent<Sender> ENABLE = info("deluxemediaplugin.log.enable");
  NullComponent<Sender> ENABLE_EMC = info("deluxemediaplugin.log.emc.enable");
  NullComponent<Sender> WELCOME = info("deluxemediaplugin.log.finish");
  NullComponent<Sender> DISABLE = info("deluxemediaplugin.log.disable");
  NullComponent<Sender> DISABLE_EMC = info("deluxemediaplugin.log.emc.disable");
  NullComponent<Sender> DISABLE_COMMAND = info("deluxemediaplugin.log.command.disable");
  NullComponent<Sender> DISABLE_BOT = info("deluxemediaplugin.log.discord.disable");
  NullComponent<Sender> DISABLE_TASK = info("deluxemediaplugin.log.native.disable");
  NullComponent<Sender> GOODBYE = info("deluxemediaplugin.log.good-bye");
  NullComponent<Sender> ENABLED_EMC = info("deluxemediaplugin.log.emc.enabled");
  NullComponent<Sender> DESERIALIZED_DATA = info("deluxemediaplugin.log.persistent.enable");
  NullComponent<Sender> ENABLE_COMMAND = info("deluxemediaplugin.log.command.enable");
  NullComponent<Sender> ENABLE_METRICS = info("deluxemediaplugin.log.metrics.enable");
  NullComponent<Sender> LATEST_VERSION = info("deluxemediaplugin.log.update.latest");
  NullComponent<Sender> INVALID_DESERIALIZATION = error("deluxemediaplugin.log.persistent.error");
  NullComponent<Sender> INVALID_EMC = error("deluxemediaplugin.log.emc.error");
  NullComponent<Sender> INVALID_INPUT = error("deluxemediaplugin.command.video.load.invalid-input");
  NullComponent<Sender> INVALID_RESOURCEPACK = error("deluxemediaplugin.command.video.load.resourcepack.unloaded");
  NullComponent<Sender> INVALID_DISCORD_CREDENTIALS = error("deluxemediaplugin.command.video.load.invalid-discord-bot");
  NullComponent<Sender> INVALID_SENDER = error("deluxemediaplugin.command.invalid-sender");
  NullComponent<Sender> INVALID_MAP_RANGE = error("deluxemediaplugin.command.map.invalid-range");
  NullComponent<Sender> UNLOADED_VIDEO = error("deluxemediaplugin.command.video.play.no-input");
  NullComponent<Sender> PROCESSING_VIDEO = error("deluxemediaplugin.command.video.load.processing");
  NullComponent<Sender> NO_PROCESSING_VIDEO = error("deluxemediaplugin.command.video.cancel-processing.no-processing");
  NullComponent<Sender> INVALID_EXTRACTION = error("deluxemediaplugin.command.video.load.extract.error");
  NullComponent<Sender> INVALID_VIDEO = error("deluxemediaplugin.command.video.load.error");
  NullComponent<Sender> INVALID_STREAM_AUDIO_OUTPUT = error("deluxemediaplugin.command.video.load.stream.error");
  NullComponent<Sender> INVALID_TARGET_SELECTOR = error("deluxemediaplugin.command.selector.error");
  NullComponent<Sender> INVALID_HTTP_CONFIGURATION = error("deluxemediaplugin.command.video.set.http.error");
  NullComponent<Sender> INVALID_DISCORD_BOT_TOKEN = error("deluxemediaplugin.log.discord.token.error");
  NullComponent<Sender> INVALID_DISCORD_GUILD_ID = error("deluxemediaplugin.log.discord.guild.error");
  NullComponent<Sender> INVALID_DISCORD_VC_ID = error("deluxemediaplugin.log.discord.vc.error");
  NullComponent<Sender> INVALID_RESOLUTION = error("deluxemediaplugin.command.video.set.dimension.error");
  NullComponent<Sender> INVALID_FILE = error("deluxemediaplugin.command.video.load.path.error");
  NullComponent<Sender> INVALID_URL = error("deluxemediaplugin.command.video.load.url.error");
  NullComponent<Sender> INVALID_WINDOW_SUPPORTED_OS = error("deluxemediaplugin.command.video.load.window.requirement-error");
  NullComponent<Sender> HIDDEN_WINDOW = error("deluxemediaplugin.command.video.load.window.hidden-error");
  NullComponent<Sender> VLC_UNSUPPORTED = error("deluxemediaplugin.command.video.set.player-algorithm.vlc-unsupported");
  NullComponent<Sender> INVALID_SCOREBOARD_DIMENSION = error("deluxemediaplugin.command.video.set.resolution.invalid-scoreboard-dimension");
  NullComponent<Sender> RESOURCEPACK_CREATION = info("deluxemediaplugin.command.video.load.resourcepack");
  NullComponent<Sender> DISCORD_DISCONNECT = info("deluxemediaplugin.command.discord.disconnect");
  NullComponent<Sender> DISCORD_CONNECT = info("deluxemediaplugin.command.discord.connect");
  NullComponent<Sender> PAUSE_VIDEO = info("deluxemediaplugin.command.video.pause");
  NullComponent<Sender> DESTROY_VIDEO = info("deluxemediaplugin.command.video.destroy");
  NullComponent<Sender> DISCORD_AUDIO_STREAM = info("deluxemediaplugin.command.video.audio.discord");
  NullComponent<Sender> DUMP_THREADS = info("deluxemediaplugin.command.dump-threads");
  NullComponent<Sender> CANCEL_VIDEO_PROCESSING = info("deluxemediaplugin.command.video.cancel-processing");
  NullComponent<Sender> LOAD_VIDEO = info("deluxemediaplugin.command.video.load.metadata");
  NullComponent<Sender> BUILD_SCREEN = info("deluxemediaplugin.command.screen.build");
  UniComponent<Sender, String> SET_MEDIA = str ->
          format(translatable("deluxemediaplugin.command.video.load.type", GOLD)
                  .args(text(str, AQUA)));
  UniComponent<Sender, String> HTTP_SEND_LINK = mrl ->
          format(translatable("deluxemediaplugin.command.video.resourcepack.link.0", GOLD)
                  .append(translatable("deluxemediaplugin.command.video.resourcepack.link.1")
                          .style(style(AQUA, BOLD, UNDERLINED, openUrl(mrl)))));

  UniComponent<Sender, String> PLAY_VIDEO = mrl->
          format(translatable("deluxemediaplugin.command.video.play", GOLD).args(text(mrl, AQUA)));
  UniComponent<Sender, String> LOADED_MEDIA = mrl ->
          format(translatable("deluxemediaplugin.command.video.load.success", GOLD).args(text(mrl, AQUA)));
  UniComponent<Sender, String> SET_AUDIO_PLAYBACK = argument ->
          format(translatable("deluxemediaplugin.command.video.set.audio-playback", GOLD).args(text(argument, AQUA)));
  UniComponent<Sender, String> SET_DITHER_ALGORITHM = algorithm ->
          format(translatable("deluxemediaplugin.command.video.set.dither-algorithm", GOLD).args(text(algorithm, AQUA)));
  UniComponent<Sender, String> SET_PLAYER_ALGORITHM = algorithm ->
          format(translatable("deluxemediaplugin.command.video.set.player-algorithm", GOLD).args(text(algorithm, AQUA)));
  UniComponent<Sender, String> SET_VIDEO_PLAYBACK = playback ->
          format(translatable("deluxemediaplugin.command.video.set.video-playback", GOLD).args(text(playback, AQUA)));
  UniComponent<Sender, String> NEW_UPDATE_PLUGIN = update ->
          format(translatable("deluxemediaplugin.log.update.available", GOLD).args(text(update, AQUA)));
  UniComponent<Sender, String> INVALID_AUDIO_PLAYBACK = argument ->
          format(translatable("deluxemediaplugin.command.video.set.audio-playback.error", RED).args(text(argument, AQUA)));
  UniComponent<Sender, String> INVALID_DITHER_ALGORITHM = algorithm ->
          format(translatable("deluxemediaplugin.command.video.set.dither-algorithm.error", RED).args(text(algorithm, AQUA)));
  UniComponent<Sender, String> INVALID_VIDEO_PLAYBACK = playback ->
          format(translatable("deluxemediaplugin.command.video.set.video-playback.error", RED).args(text(playback, AQUA)));
  UniComponent<Sender, String> CANNOT_CHECK_UPDATES = msg ->
          format(translatable("deluxemediaplugin.log.update.error", RED).args(text(msg, AQUA)));
  UniComponent<Sender, String> INVALID_PLAYER_ALGORITHM = algorithm ->
          format(translatable("deluxemediaplugin.command.video.set.player-algorithm.error", RED));
  UniComponent<Sender, DitheringAlgorithm> INVALID_NATIVE_DITHERING_ALGORITHM = algorithm ->
          format(translatable("deluxemediaplugin.command.video.set.native-dithering.error", RED).args(text(algorithm.name(), AQUA)));
  UniComponent<Sender, Integer> GIVE_SINGLE_MAP = id ->
          format(translatable("deluxemediaplugin.command.map", GOLD).args(text(id.toString(), AQUA)));
  UniComponent<Sender, Integer> CHANGED_VIDEO_MAP_ID = id ->
          format(translatable("deluxemediaplugin.command.video.set.starting-map", GOLD).args(text(id, AQUA)));

  UniComponent<Sender, Player> SEND_RESOURCEPACK_URL = player ->
          format(translatable("deluxemediaplugin.command.video.resourcepack.result.0", GOLD)
                  .args(translatable("deluxemediaplugin.command.video.resourcepack.result.1",
                          style(AQUA, BOLD, UNDERLINED, runCommand("/video resourcepack load %s".formatted(player.getName()))))));
  BiComponent<Sender, String, byte[]> RESOURCEPACK_INFO = (url, hash) ->
          format(translatable("deluxemediaplugin.command.video.resourcepack.sent", GOLD).args(text(url, AQUA), text(new String(hash), AQUA)));
  BiComponent<Sender, DitheringAlgorithm, Boolean> SET_NATIVE_DITHERING = (algorithm, enabled) ->
          format(translatable("deluxemediaplugin.command.video.set.native-dithering", GOLD).args(text(enabled ? "on" : "off", AQUA), text(algorithm.name(), AQUA)));
  BiComponent<Sender, Integer, Integer> GIVE_MAP_RANGE = (start, end) ->
          format(translatable("deluxemediaplugin.command.map.range", GOLD).args(text(start, AQUA), text(end, AQUA)));
  BiComponent<Sender, Integer, Integer> CHANGED_VIDEO_SCREEN_DIMS = (width, height) ->
          format(translatable("deluxemediaplugin.command.video.set.resolution", GOLD).args(text(width, AQUA), text(height, AQUA)));
  BiComponent<Sender, Integer, Integer> CHANGED_ITEMFRAME_DIMS = (width, height) ->
          format(translatable("deluxemediaplugin.command.video.set.itemframe-dimension", GOLD).args(text(width, AQUA), text(height, AQUA)));
  BiComponent<Sender, String, String> INVALID_PLAYER_MEDIA = (player, media) ->
          format(translatable("deluxemediaplugin.command.video.load.player.invalid-input", RED).args(text(player, AQUA), text(media, AQUA)));

  TranslationManager MANAGER = new TranslationManager();
  static @NotNull NullComponent<Sender> error(@NotNull final String key) {
    return () -> MANAGER.render(translatable(key, RED));
  }

  static @NotNull NullComponent<Sender> info(@NotNull final String key) {
    return () -> format(translatable(key, GOLD));
  }

  static @NotNull TextComponent getCommandUsageComponent(
      @NotNull final Map<String, String> usages) {
    final TextComponent.Builder builder =
        text().append(text("------------------", AQUA)).append(newline());
    usages.forEach((key, value) -> builder.append(createUsageComponent(key, value)));
    builder.append(text("------------------", AQUA));
    return builder.build();
  }

  static @NotNull Component createUsageComponent(
      @NotNull final String key, @NotNull final String value) {
    return join(
        separator(space()), text(key, LIGHT_PURPLE), text("-", GOLD), text(value, AQUA), newline());
  }

  static @NotNull Component format(@NotNull final Component message) {
    return MANAGER.render(join(separator(space()), PREFIX.build(), message));
  }
  static @NotNull Component getComponent(@NotNull final String largeString,
      @NotNull final Function<String, Component> function) {
    final TextComponent.Builder component = text();
    final String[] split = largeString.split(System.lineSeparator());
    for (final String line : split) {
      component.append(function.apply(line));
    }
    return component.build();
  }

  @FunctionalInterface
  interface NullComponent<S extends Sender> {

    Component build();

    default void send(@NotNull final S sender) {
      sender.sendMessage(format(this.build()));
    }
  }

  @FunctionalInterface
  interface UniComponent<S extends Sender, A0> {

    Component build(A0 arg0);

    default void send(@NotNull final S sender, final A0 arg0) {
      sender.sendMessage(format(this.build(arg0)));
    }
  }

  @FunctionalInterface
  interface BiComponent<S extends Sender, A0, A1> {

    Component build(A0 arg0, A1 arg1);

    default void send(@NotNull final S sender, @NotNull final A0 arg0, @NotNull final A1 arg1) {
      sender.sendMessage(format(this.build(arg0, arg1)));
    }
  }

  @FunctionalInterface
  interface TriComponent<S extends Sender, A0, A1, A2> {

    Component build(A0 arg0, A1 arg1, A2 arg2);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2)));
    }
  }

  @FunctionalInterface
  interface QuadComponent<S extends Sender, A0, A1, A2, A3> {

    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3)));
    }
  }

  @FunctionalInterface
  interface PentaComponent<S extends Sender, A0, A1, A2, A3, A4> {

    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3,
        @NotNull final A4 arg4) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3, arg4)));
    }
  }

  @FunctionalInterface
  interface HexaComponent<S extends Sender, A0, A1, A2, A3, A4, A5> {

    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3,
        @NotNull final A4 arg4,
        @NotNull final A5 arg5) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3, arg4, arg5)));
    }
  }
}
