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

import java.util.List;
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
  NullComponent<Sender> PLUGIN_INFO = () -> text()
      .append(text("-----------------------------------", GOLD))
      .append(newline())
      .append(text("Plugin: ", GOLD, BOLD))
      .append(text("DeluxeMediaPlugin", AQUA))
      .append(newline())
      .append(text("Author: ", GOLD, BOLD))
      .append(
          text(
              "PulseBeat_02",
              style(
                  AQUA,
                  text("PulseBeat_02's Github", GOLD).asHoverEvent(),
                  openUrl("https://github.com/PulseBeat02"))))
      .append(newline())
      .append(text("Version: ", GOLD, BOLD))
      .append(text("BETA Release", AQUA))
      .append(newline())
      .append(newline())
      .append(
          text(
              "Click for Support Server",
              style(
                  GOLD,
                  BOLD,
                  openUrl("https://discord.gg/AqK5dKdUZe"),
                  text("Click for Discord Server", GOLD).asHoverEvent())))
      .append(newline())
      .append(text("-----------------------------------", GOLD))
      .append()
      .build();
  NullComponent<Sender> PLUGIN_LOGO = () -> getComponent(
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
  NullComponent<Sender> PLUGIN_PREFIX = () ->
      text()
          .color(AQUA)
          .append(
              text('['), text("DeluxeMediaPlugin", GOLD), text(']'), space(), text("»", GRAY))
          .build();
  NullComponent<Sender> ENABLE_PLUGIN = () -> format(join(separator(text(" ")),
      text("Running DeluxeMediaPlugin", AQUA),
      text("[BETA]", GOLD),
      text("1.0.0", AQUA)));
  NullComponent<Sender> EMC_INIT = () -> format(
      text("Loading EzMediaCore instance... this may take some time!", GOLD));
  NullComponent<Sender> WELCOME = () -> format(text("""
      Hello %%__USER__%%! Thank you for purchasing DeluxeMediaPlugin. For identifier purposes, this \s
       is your purchase identification code: %%__NONCE__%% - Enjoy using the plugin, and ask for \s
       support at my Discord! (https://discord.gg/MgqRKvycMC) \s
      """, GOLD));
  NullComponent<Sender> DISABLE_PLUGIN = () -> format(
      text("Disabling DeluxeMediaPlugin!", GOLD));
  NullComponent<Sender> DISABLE_EMC = () -> format(
      text("Disabled MinecraftMediaLibrary instance!", GOLD));
  NullComponent<Sender> DESERIALIZE_DATA = () -> format(
      text("Deserialized data!", GOLD));
  NullComponent<Sender> DISABLE_COMMANDS = () -> format(
      text("Disabled all commands!", GOLD));
  NullComponent<Sender> DISABLE_BOT = () -> format(
      text("Disabled Discord bot!", GOLD));
  NullComponent<Sender> CANCELLED_TASKS = () -> format(
      text("Disabled all native tasks!", GOLD));
  NullComponent<Sender> GOODBYE = () -> format(text("Good Bye! :(", GOLD));
  NullComponent<Sender> FIN_EMC_INIT = () -> format(
      text("Loaded MinecraftMediaLibrary instance!", GOLD));
  NullComponent<Sender> PERSISTENT_INIT = () -> format(
      text("Loaded persistent data!", GOLD));
  NullComponent<Sender> COMMANDS_INIT = () -> format(
      text("Registered plugin commands!", GOLD));
  NullComponent<Sender> METRICS_INIT = () -> format(
      text("Loaded Metrics data!", GOLD));
  NullComponent<Sender> PLUGIN_INIT = () -> format(
      text("Loaded DeluxeMediaPlugin!", GOLD));
  NullComponent<Sender> RUNNING_LATEST_PLUGIN = () -> format(text(
      "You are currently running the latest version of DeluxeMediaPlugin.", GOLD));
  NullComponent<Sender> ERR_PERSISTENT_INIT = () -> text(
      "An issue occurred while reading data from configuration files!", RED);
  NullComponent<Sender> ERR_EMC_SHUTDOWN = () ->
      text("EzMediaCore instance is null! Check logs for errors!",
          RED);
  NullComponent<Sender> ERR_INVALID_MRL = () ->
      text("The mrl you provided is not supported!", RED);
  NullComponent<Sender> ERR_NO_RESOURCEPACK = () ->
      text("Load a resourcepack first before running this command!", RED);
  NullComponent<Sender> ERR_INVALID_DISCORD_BOT = () -> text(
      "Invalid Discord configuration in bot.yml!", RED);
  NullComponent<Sender> ERR_PLAYER_SENDER = () ->
      text("You must be a player to execute this command!", RED);
  NullComponent<Sender> ERR_MAP_RANGE = () ->
      text("Invalid map usage! (Usage: /map [starting-id]-[ending-id])", RED);
  NullComponent<Sender> ERR_VIDEO_NOT_LOADED = () -> text("Load a video first!", RED);
  NullComponent<Sender> ERR_VIDEO_PROCESSING = () ->
      text("The video is still processing!", RED);
  NullComponent<Sender> ERR_CANCELLATION_VIDEO_PROCESSING = () ->
      text("There is no video processing!", RED);
  NullComponent<Sender> ERR_DOWNLOAD_VIDEO = () -> text("Failed to download video!", RED);
  NullComponent<Sender> ERR_LOAD_VIDEO = () -> text("Failed to load video!", RED);
  NullComponent<Sender> ERR_INVALID_AUDIO_OUTPUT = () -> text(
      "You cannot play streams without using HTTP or Discord! Proceeding to play stream without audio.",
      RED);
  NullComponent<Sender> ERR_INVALID_TARGET_SELECTOR = () -> text(
      "The target selector must be all players!", RED);
  NullComponent<Sender> ERR_HTTP_AUDIO = () ->
      text("Invalid HTTP configuration in httpaudio.yml!",
          RED);
  NullComponent<Sender> ERR_BOT_TOKEN = () ->
      text("Invalid Discord token in bot.yml!", RED);
  NullComponent<Sender> ERR_GUILD_ID = () ->
      text("Invalid Guild ID in bot.yml!", RED);
  NullComponent<Sender> ERR_VC_ID = () ->
      text("Invalid Voice Chat ID specified in bot.yml!", RED);
  NullComponent<Sender> ERR_INVALID_DIMS = () -> text("Invalid dimension usage! (Usage: [width]:[height])", RED);
  NullComponent<Sender> ERR_INVALID_PATH = () -> text(
      "The file does not exist! Make sure the path is an absolute path!",
      RED);
  NullComponent<Sender> ERR_INVALID_URL = () ->
      text("The URL is not valid!", RED);
  NullComponent<Sender> ERR_INVALID_OS = () -> text(
      "Window capture only works on Window's devices!",
      RED);
  NullComponent<Sender> ERR_INVALID_WINDOW = () ->
      text("The window is not visible!", RED);
  NullComponent<Sender> ERR_VLC_UNSUPPORTED = () -> text(
      "VLC is not supported on this server!",
      RED);
  NullComponent<Sender> ERR_SCOREBOARD_DIMENSION = () ->
      text("Scoreboard width must be between 0 and 32 and scoreboard height must be between 0 and 16!", RED);

  NullComponent<Sender> ERR_INVALID_SELECTOR = () -> text("Invalid target selector!", RED);
  NullComponent<Sender> RESOURCEPACK_CREATION = () -> format(text(
      "Creating a resourcepack for audio...",
      GOLD));
  NullComponent<Sender> DISCORD_DISCONNECT = () -> format(
      text("Disconnected from voice channel!", GOLD));
  NullComponent<Sender> DISCORD_CONNECT = () -> format(
      text("Connected to voice channel!", GOLD));
  NullComponent<Sender> FFMPEG_EXECUTION = () -> format(text("Executed FFmpeg command!", GOLD));
  NullComponent<Sender> RESET_FFMPEG_ARGS = () -> format(text("Reset all FFmpeg arguments!", GOLD));
  NullComponent<Sender> PAUSE_VIDEO = () -> format(text("Paused the video!", GOLD));
  NullComponent<Sender> DESTROY_VIDEO = () -> format(
      text("Destroyed the video!", GOLD));
  NullComponent<Sender> DISCORD_AUDIO_STREAM = () -> format(
      text("Began playing audio into Discord voice chat (5 seconds)!", GOLD));
  NullComponent<Sender> DUMP_THREADS = () -> format(
      text("Created thread dump in console!", GOLD));
  NullComponent<Sender> CANCELLED_VIDEO_PROCESSING = () -> format(
      text("Cancelled the video loading!", GOLD));
  NullComponent<Sender> LOADING_VIDEO = () -> format(
      text("Extracting media metadata...", GOLD));
  NullComponent<Sender> BUILT_SCREEN = () -> format(
      text("Built your new screen!", GOLD));
  UniComponent<Sender, String> SET_MEDIA = (str) -> format(
      text("Set media to %s!".formatted(str), GOLD));
  UniComponent<Sender, String> ADD_FFMPEG_ARG = (str) -> format(join(
      separator(space()),
      text("Added arguments", GOLD),
      text(str, AQUA),
      text("to the command.", GOLD)));
  UniComponent<Sender, String> REMOVE_FFMPEG_ARG = (str) -> format(join(
      separator(space()),
      text("Removed arguments", GOLD),
      text(str, AQUA),
      text("from the command.", GOLD)));
  UniComponent<Sender, String> HTTP_SEND_LINK = (mrl) ->
      format(text()
          .append(text("Click ", GOLD)).append(text(
              "this message",
              style(
                  AQUA,
                  BOLD,
                  UNDERLINED,
                  openUrl(mrl),
                  text("Click to get the link!", GOLD)
                      .asHoverEvent()))).append(text(" to retrieve the HTTP link!", GOLD))
          .build());
  UniComponent<Sender, String> PLAY_VIDEO = (mrl) -> format(
      text("Started playing %s".formatted(mrl), GOLD));
  UniComponent<Sender, String> LOADED_MEDIA = (mrl) -> format(
      text("Loaded media %s!".formatted(mrl), GOLD));
  UniComponent<Sender, String> SET_AUDIO_PLAYBACK = (argument) -> format(
      join(separator(space()), text("Set audio playback to", GOLD),
          text(argument, AQUA)));
  UniComponent<Sender, String> SET_DITHER_ALGORITHM = (algorithm) -> format(
      join(separator(space()), text("Set dither algorithm to", GOLD),
          text(algorithm, AQUA)));
  UniComponent<Sender, String> SET_PLAYER_ALGORITHM = (algorithm) -> format(
      join(separator(space()), text("Set player algorithm to", GOLD),
          text(algorithm, AQUA)));
  UniComponent<Sender, String> SET_VIDEO_PLAYBACK = (playback) -> format(
      join(separator(space()), text("Set video playback to", GOLD),
          text(playback, AQUA)));
  UniComponent<Sender, String> NEW_UPDATE_PLUGIN = (update) -> format(text(
      "There is a new update available! (%s)".formatted(update), GOLD));
  UniComponent<Sender, String> ERR_INVALID_AUDIO_PLAYBACK = (argument) -> format(text(
      "Could not find audio playback %s".formatted(argument), RED));
  UniComponent<Sender, String> ERR_INVALID_DITHER_ALGORITHM = (algorithm) -> format(text(
      "Could not find dithering algorithm %s".formatted(algorithm), RED));
  UniComponent<Sender, String> ERR_INVALID_VIDEO_PLAYBACK = (playback) -> format(
      text("Could not find video playback %s".formatted(playback), RED));
  UniComponent<Sender, String> ERR_CANNOT_CHECK_UPDATES = (msg) -> format(
      text("Cannot look for updates: %s".formatted(msg), RED));
  UniComponent<Sender, String> ERR_INVALID_PLAYER_ALGORITHM = (algorithm) -> format(text(
      "Could not find player algorithm %s".formatted(algorithm), RED));
  UniComponent<Sender, DitheringAlgorithm> ERR_INVALID_NATIVE_DITHERING_ALGORITHM = (algorithm) -> format(
      text(
          "Dithering algorithm %s is not natively supported!".formatted(algorithm.name())
      ));
  UniComponent<Sender, List<String>> LIST_FFMPEG_ARGS = (list) -> format(join(
      separator(space()),
      text("Current arguments:", GOLD),
      text(list.toString(), AQUA)));
  UniComponent<Sender, Integer> GIVE_MAP_ID = (id) -> format(join(
      separator(space()), text("Gave map with id", GOLD), text(id, AQUA)));
  UniComponent<Sender, Integer> CHANGED_VIDEO_MAP_ID = (id) -> format(join(
      separator(space()),
      text("Set starting map id to", GOLD),
      text(id, AQUA)));
  UniComponent<Sender, Player> SEND_RESOURCEPACK_URL = (player) -> format(text()
      .append(text("Loaded resourcepack for all players! Click ", GOLD))
      .append(
          text(
              "this message",
              style(
                  AQUA,
                  BOLD,
                  UNDERLINED,
                  runCommand(
                      "/video resourcepack load %s"
                          .formatted(player.getName())),
                  text("Click to get the resourcepack!", GOLD)
                      .asHoverEvent())))
      .append(text(" to retrieve the link!", GOLD))
      .build());
  UniComponent<Sender, String> FFMPEG_PROCESS = (str) ->
      text()
          .color(AQUA)
          .append(text('['), text("External Process", GOLD), text(']'), space(), text("»", GRAY))
          .append(text(" %s".formatted(str)))
          .build();
  BiComponent<Sender, String, byte[]> SENT_RESOURCEPACK = (url, hash) -> format(text(
      "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash))));
  BiComponent<Sender, DitheringAlgorithm, Boolean> SET_NATIVE_DITHERING = (algorithm, enabled) -> format(
      text(
          "Set native dithering %s for dithering algorithm %s".formatted(enabled ? "on" : "off",
              algorithm.name())
      ));
  BiComponent<Sender, String, Integer>
      ADD_FFMPEG_ARG_INDX = (str, index) ->
      format(join(
          separator(space()),
          text("Added arguments", GOLD),
          text(str, AQUA),
          text("  the FFmpeg command at index", GOLD),
          text(index, AQUA)));
  BiComponent<Sender, String, Integer> REMOVE_FFMPEG_ARG_INDX = (str, index) ->
      format(join(
          separator(space()),
          text("Removed arguments", GOLD),
          text(str, AQUA),
          text("from the FFmpeg command at index", GOLD),
          text(index, AQUA)));
  BiComponent<Sender, Integer, Integer> GAVE_MAP_RANGE = (start, end) ->
      format(join(
          separator(space()),
          text("Gave maps between IDs", GOLD),
          text(start, AQUA),
          text("and", GOLD),
          text(end, AQUA)));
  BiComponent<Sender, Integer, Integer> CHANGED_VIDEO_SCREEN_DIMS = (width, height) ->
      format(join(
          separator(space()),
          text("Set resolution to", GOLD),
          text("%d:%d".formatted(width, height), AQUA),
          text("(width:height)", GOLD)));
  BiComponent<Sender, Integer, Integer> CHANGED_ITEMFRAME_DIMS = (width, height) ->
      format(join(
          separator(space()),
          text("Set itemframe dimensions to", GOLD),
          text("%d:%d".formatted(width, height), AQUA),
          text("(width:height)", GOLD)));
  BiComponent<Sender, String, String> ERR_INVALID_PLAYER_MEDIA = (player, media) -> format(
      text("Invalid player! %s does not support %s!", RED));

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
    return join(separator(space()), PLUGIN_PREFIX.build(), message);
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
