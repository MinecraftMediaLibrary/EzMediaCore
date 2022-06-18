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

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

import io.github.pulsebeat02.deluxemediaplugin.command.video.output.DitheringAlgorithm;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Locale {

  UniComponent<Sender, String> REQUIRED_ARGUMENT =
      (argument) ->
          text()
              .color(DARK_GRAY)
              .append(text("<"))
              .append(text(argument, GRAY))
              .append(text(">"))
              .build();

  UniComponent<Sender, String> OPTIONAL_ARGUMENT =
      (argument) ->
          text()
              .color(DARK_GRAY)
              .append(text("["))
              .append(text(argument, GRAY))
              .append(text("]"))
              .build();
  NullComponent<Sender> PLUGIN_AUTHORS = () -> text()
      .append(text("-----------------------------------", GOLD))
      .append(newline())
      .append(text("Plugin: ", GOLD, BOLD))
      .append(text("DeluxeMediaPlugin", AQUA))
      .append(newline())
      .append(text("Authors: ", GOLD, BOLD))
      .append(
          text(
              "PulseBeat_02",
              style(
                  AQUA,
                  text("PulseBeat_02's Github", GOLD).asHoverEvent(),
                  openUrl("https://github.com/PulseBeat02"))))
      .append(text(", ", GOLD))
      .append(
          text(
              "itxfrosty",
              style(
                  AQUA,
                  text("itxfrosty's Github", GOLD).asHoverEvent(),
                  openUrl("https://github.com/itxfrosty"))))
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
      text("DeluxeMediaPlugin is shutting down!", GOLD));
  NullComponent<Sender> DISABLE_EMC = () -> format(
      text("Successfully shutdown MinecraftMediaLibrary instance!", GOLD));
  NullComponent<Sender> DESERIALIZE_DATA = () -> format(
      text("Successfully deserialized data!", GOLD));
  NullComponent<Sender> DISABLE_COMMANDS = () -> format(
      text("Successfully disabled commands!", GOLD));
  NullComponent<Sender> DISABLE_BOT = () -> format(
      text("Successfully disabled Discord bot! (If running)", GOLD));
  NullComponent<Sender> CANCELLED_TASKS = () -> format(
      text("Successfully disabled all native tasks!", GOLD));
  NullComponent<Sender> GOODBYE = () -> format(text("Good Bye! :(", GOLD));
  NullComponent<Sender> FIN_EMC_INIT = () -> format(
      text("Finished loading MinecraftMediaLibrary instance!", GOLD));
  NullComponent<Sender> FIN_PERSISTENT_INIT = () -> format(
      text("Finished loading persistent data!", GOLD));
  NullComponent<Sender> FIN_COMMANDS_INIT = () -> format(
      text("Finished registering plugin commands!", GOLD));
  NullComponent<Sender> FIN_METRICS_INIT = () -> format(
      text("Finished loading Metrics data!", GOLD));
  NullComponent<Sender> FIN_PLUGIN_INIT = () -> format(
      text("Finished loading DeluxeMediaPlugin!", GOLD));
  NullComponent<Sender> RUNNING_LATEST_PLUGIN = () -> format(text(
      "You are currently running the latest version of DeluxeMediaPlugin.", GOLD));
  NullComponent<Sender> ERR_EMC_INIT = () -> format(
      text("There was a severe issue while loading the EzMediaCore instance!",
          RED));
  NullComponent<Sender> ERR_PERSISTENT_INIT = () -> format(text(
      "A severe issue occurred while reading data from configuration files!", RED));
  NullComponent<Sender> ERR_EMC_SHUTDOWN = () -> format(
      text("EzMediaCore instance is null... something fishy is going on.",
          RED));
  NullComponent<Sender> ERR_NO_MRL = () -> format(text("File or URL not specified!", RED));
  NullComponent<Sender> ERR_INVALID_MRL = () -> format(
      text("Invalid MRL link! Not supported!", RED));
  NullComponent<Sender> ERR_RESOURCEPACK_WRAP = () -> format(
      text("Failed to wrap resourcepack!", RED));
  NullComponent<Sender> ERR_NO_RESOURCEPACK = () -> format(
      text("Please load a resourcepack first!", RED));
  NullComponent<Sender> ERR_INVALID_AUDIO_STATE = () -> format(text(
      "Please wait for the previous audio to extract first before loading another one!", RED));
  NullComponent<Sender> ERR_INVALID_DISCORD_BOT = () -> format(text(
      "Discord bot not setup yet or invalid settings in bot.yml!", RED));
  NullComponent<Sender> ERR_INVALID_DISCORD_BOT_VPN = () -> format(text(
      "Are you using a VPN perhaps? JDA doesn't like VPNs.", RED));
  NullComponent<Sender> ERR_PLAYER_SENDER = () -> format(
      text("You must be a player to execute this command!", RED));
  NullComponent<Sender> ERR_MAP_RANGE = () -> format(
      text("Invalid format! Must follow [starting-id]-[ending-id]", RED));
  NullComponent<Sender> ERR_VIDEO_NOT_LOADED = () -> format(text("Video not loaded!", RED));
  NullComponent<Sender> ERR_VIDEO_PROCESSING = () -> format(
      text("Video is still processing!", RED));
  NullComponent<Sender> ERR_CANCELLATION_VIDEO_PROCESSING = () -> format(
      text("You aren't processing a video!", RED));
  NullComponent<Sender> ERR_DOWNLOAD_VIDEO = () -> format(text("Failed to download video!", RED));
  NullComponent<Sender> ERR_LOAD_VIDEO = () -> format(text("Failed to load video!", RED));
  NullComponent<Sender> ERR_INVALID_AUDIO_OUTPUT = () -> format(text(
      "You cannot play streams without using Discord or a dynamic audio player with audio. Proceeding to play without audio.",
      RED));
  NullComponent<Sender> ERR_INVALID_TARGET_SELECTOR = () -> format(text(
      "The target selector you chose contains entities that aren't players!", RED));
  NullComponent<Sender> ERR_DEVELOPMENT_FEATURE = () -> format(text(
      "This feature is current being developed! Stay tuned and join the Discord for updates!",
      RED));
  NullComponent<Sender> ERR_HTTP_AUDIO = () -> format(
      text("HTTP audio information provided in httpaudio.yml is invalid!",
          RED));
  NullComponent<Sender> ERR_BOT_TOKEN = () -> format(
      text("Bot token not specified in bot.yml!", RED));
  NullComponent<Sender> ERR_GUILD_TOKEN = () -> format(
      text("Guild token not specified in bot.yml!", RED));
  NullComponent<Sender> ERR_VC_ID = () -> format(
      text("Voice Chat Identifier not specified in bot.yml!", RED));
  NullComponent<Sender> ERR_INVALID_DIMS = () -> format(text("Invalid dimensions!", RED));
  NullComponent<Sender> ERR_INVALID_PATH = () -> format(text("The file at the path you specified does not exist! Make sure the path is an absolute path!", RED));
  NullComponent<Sender> ERR_INVALID_URL = () -> format(text("The URL you specified is not valid! Make sure you are able to connect to it!", RED));
  NullComponent<Sender> ERR_INVALID_OS = () -> format(text("Unfortunately, window capture only works on FFmpeg and Window's devices. Sorry about the inconvenience.", RED));
  NullComponent<Sender> ERR_INVALID_WINDOW = () -> format(text("The window you specified is not visible or simply does not exist!", RED));
  NullComponent<Sender> ERR_VLC_UNSUPPORTED = () -> format(text("Sorry, but VLC isn't supported for your enviornment. Please use snap to download it for your enviornment!", RED));
  NullComponent<Sender> START_AUDIO = () -> format(text("Started playing audio!", GOLD));
  NullComponent<Sender> PAUSE_AUDIO = () -> format(text("Stopped playing audio!", GOLD));
  NullComponent<Sender> RESUME_AUDIO = () -> format(text("Resumed the video!", GOLD));
  NullComponent<Sender> CREATE_RESOURCEPACK = () -> format(text(
      "Creating a resourcepack for audio. Depending on the length of the video, it make take some time.",
      GOLD));
  NullComponent<Sender> DC_DISCORD = () -> format(
      text("Successfully disconnected from voice channel!", GOLD));
  NullComponent<Sender> C_DISCORD = () -> format(
      text("Successfully connected to voice channel!", GOLD));
  NullComponent<Sender> PAUSED_TRACK_DISCORD = () -> format(
      text("Successfully paused track!", GOLD));
  NullComponent<Sender> RESUMED_TRACK_DISCORD = () -> format(
      text("Successfully resumed track!", GOLD));
  NullComponent<Sender> DITHERING_OPTIONS = () -> format(text("Dithering Options ->", GOLD)
      .append(getComponent(DitheringAlgorithm.class,
          (value) -> newline().append(text(value.name(), AQUA)))));
  NullComponent<Sender> FFMPEG_EXEC = () -> format(text("Executed FFmpeg command!", GOLD));
  NullComponent<Sender> RESET_FFMPEG_ARGS = () -> format(text("Reset all FFmpeg arguments!", GOLD));
  NullComponent<Sender> LOAD_IMG = () -> format(text("Loading image...", GOLD));
  NullComponent<Sender> PURGE_ALL_MAPS_VERIFY = () -> format(text(
      "Are you sure you want to purge all maps? Type YES (all caps) if you would like to continue...",
      GOLD));
  NullComponent<Sender> PURGED_ALL_MAPS = () -> format(
      text("Successfully purged all images!", GOLD));
  NullComponent<Sender> CANCELLED_PURGE_ALL_MAPS = () -> format(
      text("Cancelled purge of all images!", GOLD));
  NullComponent<Sender> PAUSE_VIDEO = () -> format(text("Stopped the video!", GOLD));
  NullComponent<Sender> RELEASE_VIDEO = () -> format(
      text("Successfully destroyed the current video!", GOLD));
  NullComponent<Sender> SETUP_RESOURCEPACK = () -> format(text(
      "Setting up resourcepack for resuming... this may take a while depending on how large the audio file is.",
      GOLD));
  NullComponent<Sender> DISCORD_AUDIO_STREAM = () -> format(
      text("Started playing audio into Discord voice chat!", GOLD));
  NullComponent<Sender> DUMP_THREADS = () -> format(
      text("Created thread dump! Look in console for more details.", GOLD));
  NullComponent<Sender> CANCELLED_VIDEO_PROCESSING = () -> format(
      text("Successfully cancelled the video loading process!", GOLD));
  NullComponent<Sender> LOADING_VIDEO = () -> format(
      text("Initializing and reading media...", GOLD));
  NullComponent<Sender> BUILT_SCREEN = () -> format(
      text("Successfully built your new screen!", GOLD));
  UniComponent<Sender, String> SET_MEDIA = (str) -> format(text("Set media to %s!".formatted(str), GOLD));
  UniComponent<Sender, String>
      DREW_IMG = (mrl) -> format(text("Successfully drew image with mrl %s".formatted(mrl), GOLD));
  UniComponent<Sender, String> START_TRACK_DISCORD = (mrl) -> format(
      text("Successfully started audio on MRL %s!".formatted(mrl), GOLD));
  UniComponent<Sender, String> ADD_FFMPEG_ARG = (str) -> format(join(
      separator(space()),
      text("Added arguments", GOLD),
      text(str, AQUA),
      text("to the FFmpeg command.", GOLD)));
  UniComponent<Sender, String> REMOVE_FFMPEG_ARG = (str) -> format(join(
      separator(space()),
      text("Removed arguments", GOLD),
      text(str, AQUA),
      text("from the FFmpeg command.", GOLD)));
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
                      .asHoverEvent()))).append(text(" to retrieve the audio HTTP link!", GOLD))
          .build());
  UniComponent<Sender, String> STARTING_VIDEO = (mrl) -> format(
      text("Starting Video on MRL %s".formatted(mrl), GOLD));
  UniComponent<Sender, String> LOADED_MEDIA = (mrl) -> format(
      text("Successfully loaded media %s!".formatted(mrl), GOLD));
  UniComponent<Sender, String> SET_AUDIO_PLAYBACK = (argument) -> format(text(
      "Successfully set the audio playback to %s".formatted(argument), GOLD));
  UniComponent<Sender, String> SET_DITHER_ALGORITHM = (algorithm) -> format(
      join(separator(space()), text("Set dither algorithm to", GOLD),
          text(algorithm, AQUA)));
  UniComponent<Sender, String> SET_PLAYER_ALGORITHM = (algorithm) -> format(
      join(separator(space()), text("Set player algorithm to", GOLD),
          text(algorithm, AQUA)));
  UniComponent<Sender, String> SET_VIDEO_PLAYBACK = (playback) -> format(
      join(separator(space()), text("Set video playback to", GOLD),
          text(playback, AQUA)));
  UniComponent<Sender, String> EXTERNAL_PROCESS = (line) -> format(join(separator(space()), text()
          .color(AQUA)
          .append(text('['), text("External Process", GOLD), text(']'), space(), text("»", GRAY)),
      text(line, GOLD)));
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
  UniComponent<Sender, DitheringAlgorithm> ERR_INVALID_NATIVE_DITHERING_ALGORITHM = (algorithm) -> format(text(
      "The dithering algorithm %s is not natively supported!".formatted(algorithm.name())
  ));
  UniComponent<Sender, List<String>> LIST_FFMPEG_ARGS = (list) -> format(join(
      separator(space()),
      text("Current FFmpeg arguments:", GOLD),
      text(list.toString(), AQUA)));
  UniComponent<Sender, Integer>
      PURGE_MAP = (id) ->
      format(join(
          separator(space()),
          text("Successfully purged all maps with id", GOLD),
          text(id, AQUA)));
  UniComponent<Sender, Integer> GIVE_MAP_ID = (id) -> format(join(
      separator(space()), text("Gave map with id", GOLD), text(id, AQUA)));
  UniComponent<Sender, Integer> CHANGED_VIDEO_MAP_ID = (id) -> format(join(
      separator(space()),
      text("Set starting map id to", GOLD),
      text(id, AQUA)));
  UniComponent<Sender, Long> RESUMING_VIDEO_MS = (ms) -> format(
      text("Resuming Video at %s Milliseconds!", GOLD));
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
                      "/video load resourcepack %s"
                          .formatted(player.getName())),
                  text("Click to get the resourcepack!", GOLD)
                      .asHoverEvent())))
      .append(text(" to retrieve the resourcepack", GOLD))
      .build());
  UniComponent<Sender, String> FFMPEG_PROCESS = (str) ->
      text()
          .color(AQUA)
          .append(text('['), text("External Process", GOLD), text(']'), space(), text("»", GRAY))
          .append(text(" %s".formatted(str)))
          .build();
  UniComponent<Sender, Map<String, String>> INFO_CMD_USAGE = Locale::getCommandUsageComponent;
  BiComponent<Sender, String, byte[]>
      FIN_RESOURCEPACK_INIT = (url, hash) -> format(text(
      "Loaded Resourcepack Successfully! (URL: %s, Hash: %s)".formatted(url, new String(hash))));
  BiComponent<Sender, String, byte[]> SENT_RESOURCEPACK = (url, hash) -> format(text(
      "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash))));
  BiComponent<Sender, DitheringAlgorithm, Boolean> SET_NATIVE_DITHERING = (algorithm, enabled) -> format(text(
      "Set native dithering %s for dithering algorithm %s".formatted(enabled ? "on" : "off", algorithm.name())
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
  BiComponent<Sender, Integer, Integer>
      CHANGED_IMG_DIMS = (width, height) -> format(text(
      "Changed itemframe dimensions to %d:%d (width:height)".formatted(width, height)));
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
          text("Set screen resolution to", GOLD),
          text("%d:%d".formatted(width, height), AQUA),
          text("(width:height)", GOLD)));
  BiComponent<Sender, Integer, Integer> CHANGED_ITEMFRAME_DIMS = (width, height) ->
      format(join(
          separator(space()),
          text("Set itemframe map dimensions to", GOLD),
          text("%d:%d".formatted(width, height), AQUA),
          text("(width:height)", GOLD)));
  BiComponent<Sender, String, String> ERR_INVALID_PLAYER_MEDIA = (player, media) -> format(text("Invalid player! %s does not support %s!", RED));

  static @NotNull TextComponent getCommandUsageComponent(@NotNull final Map<String, String> usages) {
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

  static <T extends Enum<T>> @NotNull Component getComponent(@NotNull final Class<T> clazz,
      @NotNull final Function<T, Component> function) {
    final T[] arr = clazz.getEnumConstants();
    final TextComponent.Builder component = text();
    for (final T value : arr) {
      component.append(function.apply(value));
    }
    return component.build();
  }

  static <E> @NotNull Component getComponent(
      @NotNull final Collection<E> collection,
      @NotNull final Function<E, Component> function) {
    final TextComponent.Builder component = text();
    for (final E value : collection) {
      component.append(function.apply(value));
    }
    return component.build();
  }

  /*


    _____       _                __  __          _ _       _____  _             _
 |  __ \     | |              |  \/  |        | (_)     |  __ \| |           (_)
 | |  | | ___| |_   ___  _____| \  / | ___  __| |_  __ _| |__) | |_   _  __ _ _ _ __
 | |  | |/ _ \ | | | \ \/ / _ \ |\/| |/ _ \/ _` | |/ _` |  ___/| | | | |/ _` | | '_ \
 | |__| |  __/ | |_| |>  <  __/ |  | |  __/ (_| | | (_| | |    | | |_| | (_| | | | | |
 |_____/ \___|_|\__,_/_/\_\___|_|  |_|\___|\__,_|_|\__,_|_|    |_|\__,_|\__, |_|_| |_|
                                                                         __/ |
                                                                        |___/

   */

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
