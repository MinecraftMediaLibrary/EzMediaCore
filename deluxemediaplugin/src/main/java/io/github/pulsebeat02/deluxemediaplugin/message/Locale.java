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

package io.github.pulsebeat02.deluxemediaplugin.message;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherSetting;
import io.github.pulsebeat02.deluxemediaplugin.command.image.ImageMrlType;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Locale {

  Component NEW_LINE = Component.newline();
  Component SPACE = Component.space();

  UniComponent<Sender, String> REQUIRED_ARGUMENT =
      argument ->
          text()
              .color(DARK_GRAY)
              .append(text("<"))
              .append(text(argument, GRAY))
              .append(text(">"))
              .build();

  UniComponent<Sender, String> OPTIONAL_ARGUMENT =
      argument ->
          text()
              .color(DARK_GRAY)
              .append(text("["))
              .append(text(argument, GRAY))
              .append(text("]"))
              .build();

  NullComponent<Sender>

      ENABLE_PLUGIN = () -> join(separator(text(" ")),
      text("Running DeluxeMediaPlugin", AQUA),
      text("[BETA]", GOLD),
      text("1.0.0", AQUA)),
      EMC_INIT = () -> text("Loading EzMediaCore instance... this may take some time!"),
      WELCOME = () -> text("""
          Hello %%__USER__%%! Thank you for purchasing DeluxeMediaPlugin. For identifier purposes, this
           is your purchase identification code: %%__NONCE__%% - Enjoy using the plugin, and ask for
           support at my Discord! (https://discord.gg/MgqRKvycMC)
          """),

  DISABLE_PLUGIN = () -> text("DeluxeMediaPlugin is shutting down!"),
      GOOD_EMC_SHUTDOWN = () -> text("Successfully shutdown MinecraftMediaLibrary instance!"),
      GOODBYE = () -> text("Good Bye! :("),

  FIN_EMC_INIT = () -> text("Finished loading MinecraftMediaLibrary instance!"),
      FIN_PERSISTENT_INIT = () -> text("Finished loading persistent data!"),
      FIN_COMMANDS_INIT = () -> text("Finished registering plugin commands!"),
      FIN_METRICS_INIT = () -> text("Finished loading Metrics data!"),
      FIN_PLUGIN_INIT = () -> text("Finished loading DeluxeMediaPlugin!"),

  RUNNING_LATEST_PLUGIN = () -> text(
      "You are currently running the latest version of DeluxeMediaPlugin."),

  ERR_EMC_INIT = () -> text("There was a severe issue while loading the EzMediaCore instance!",
      RED),
      ERR_PERSISTENT_INIT = () -> text(
          "A severe issue occurred while reading data from configuration files!", RED),
      ERR_EMC_SHUTDOWN = () -> text("EzMediaCore instance is null... something fishy is going on.",
          RED),
      ERR_NO_MRL = () -> text("File or URL not specified!", RED),
      ERR_INVALID_MRL = () -> text("Invalid MRL link! Not supported!", RED),
      ERR_RESOURCEPACK_WRAP = () -> text("Failed to wrap resourcepack!", RED),
      ERR_NO_RESOURCEPACK = () -> text("Please load a resourcepack first!", RED),
      ERR_INVALID_AUDIO_STATE = () -> text(
          "Please wait for the previous audio to extract first before loading another one!", RED),
      ERR_INVALID_DISCORD_BOT = () -> text(
          "Discord bot not setup yet or invalid settings in bot.yml!", RED),
      ERR_PLAYER_SENDER = () -> text("You must be a player to execute this command!", RED),
      ERR_INVALID_EXTENSION = () -> text(
          "Image doesn't match any supported extensions! (%s)".formatted(ImageMrlType.EXTENSIONS)),
      ERR_IMG_SET = () -> text("Failed to set image file!", RED),
      ERR_IMAGE_NOT_LOADED = () -> text("The image you request purge from the map is not loaded!",
          RED),
      ERR_MAP_RANGE = () -> text("Invalid format! Must follow [starting-id]-[ending-id]", RED),
      ERR_VIDEO_NOT_LOADED = () -> text("Video not loaded!", RED),
      ERR_VIDEO_PROCESSING = () -> text("Video is still processing!", RED),
      ERR_CANCELLATION_VIDEO_PROCESSING = () -> text("You aren't loading a video!", RED),
      ERR_DOWNLOAD_VIDEO = () -> text("Failed to download video!", RED),
      ERR_LOAD_VIDEO = () -> text("Failed to load video!", RED),
      ERR_INVALID_AUDIO_OUTPUT = () -> text(
          "You cannot play streams without using Discord or a dynamic audio player with audio. Proceeding to play without audio.",
          RED),
      ERR_INVALID_TARGET_SELECTOR = () -> text(
          "The target selector you chose contains entities that aren't players!", RED),
      ERR_DEVELOPMENT_FEATURE = () -> text(
          "This feature is current being developed! Stay tuned and join the Discord for updates!",
          RED),
      ERR_HTTP_AUDIO = () -> text("HTTP audio information provided in httpaudio.yml is invalid!",
          RED),
      ERR_BOT_TOKEN = () -> text("Bot token not specified in bot.yml!", RED),
      ERR_GUILD_TOKEN = () -> text("Guild token not specified in bot.yml!", RED),
      ERR_VC_ID = () -> text("Voice Chat Identifier not specified in bot.yml!", RED),

  START_AUDIO = () -> text("Started playing audio!"),
      PAUSE_AUDIO = () -> text("Stopped playing audio!"),
      RESUME_AUDIO = () -> text("Resumed the video!"),
      CREATE_RESOURCEPACK = () -> text(
          "Creating a resourcepack for audio. Depending on the length of the video, it make take some time."),

  DC_DISCORD = () -> text("Successfully disconnected from voice channel!"),
      C_DISCORD = () -> text("Successfully connected to voice channel!"),
      PAUSED_TRACK_DISCORD = () -> text("Successfully paused track!"),
      RESUMED_TRACK_DISCORD = () -> text("Successfully resumed track!"),

  DITHERING_OPTIONS = () -> text("Dithering Options ->")
      .append(getComponent(DitherSetting.class,
          (value) -> text(value.getName(), AQUA).append(newline()))),

  FFMPEG_EXEC = () -> text("Executed FFmpeg command!"),
      RESET_FFMPEG_ARGS = () -> text("Reset all FFmpeg arguments!"),

  LOAD_IMG = () -> text("Loading image..."),
      PURGE_ALL_MAPS_VERIFY = () -> text(
          "Are you sure you want to purge all maps? Type YES (all caps) if you would like to continue..."),
      PURGED_ALL_MAPS = () -> text("Successfully purged all images!"),
      CANCELLED_PURGE_ALL_MAPS = () -> text("Cancelled purge of all images!"),

  PAUSE_VIDEO = () -> text("Stopped the video!"),
      RELEASE_VIDEO = () -> text("Successfully destroyed the current video!"),
      SETUP_RESOURCEPACK = () -> text(
          "Setting up resourcepack for resuming... this may take a while depending on how large the audio file is."),
      DISCORD_AUDIO_STREAM = () -> text("Started playing audio into Discord voice chat!"),

  DUMP_THREADS = () -> text("Created thread dump! Look in console for more details."),

  CANCELLED_VIDEO_PROCESSING = () -> text("Successfully cancelled the video loading process!"),
      LOADING_VIDEO = () -> text("Initializing and reading media..."),

  PLUGIN_AUTHORS = () -> text()
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

  UniComponent<Sender, String>
      DREW_IMG = (mrl) -> text("Successfully drew image with mrl %s".formatted(mrl)),
      START_TRACK_DISCORD = (mrl) -> text("Successfully started audio on MRL %s!".formatted(mrl)),
      ADD_FFMPEG_ARG = (str) -> join(
          separator(space()),
          text("Added arguments", GOLD),
          text(str, AQUA),
          text("to the FFmpeg command.", GOLD)),
      REMOVE_FFMPEG_ARG = (str) -> join(
          separator(space()),
          text("Removed arguments", GOLD),
          text(str, AQUA),
          text("from the FFmpeg command.", GOLD)),
      HTTP_SEND_LINK = (mrl) ->
          text()
              .append(text("Click ", GOLD)).append(text(
                  "this message",
                  style(
                      AQUA,
                      BOLD,
                      UNDERLINED,
                      openUrl(mrl),
                      text("Click to get the link!", GOLD)
                          .asHoverEvent()))).append(text(" to retrieve the audio HTTP link!", GOLD))
              .build(),
      STARTING_VIDEO = (mrl) -> text("Starting Video on MRL %s".formatted(mrl)),
      LOADED_MEDIA = (mrl) -> text("Successfully loaded media %s!".formatted(mrl)),
      SET_AUDIO_TYPE = (argument) -> text(
          "Successfully set the audio type to %s".formatted(argument)),
      SET_DITHER_TYPE = (algorithm) -> join(separator(space()), text("Set dither type to", GOLD),
          text(algorithm, AQUA)),
      SET_VIDEO_TYPE = (mode) -> join(separator(space()), text("Set video mode to", GOLD),
          text(mode, AQUA)),
      EXTERNAL_PROCESS = (line) -> join(separator(space()), text()
              .color(AQUA)
              .append(text('['), text("External Process", GOLD), text(']'), space(), text("»", GRAY)),
          text(line, GOLD)),
      NEW_UPDATE_PLUGIN = (update) -> text(
          "There is a new update available! (%s)".formatted(update)),

  ERR_INVALID_AUDIO_TYPE = (argument) -> text(
      "Could not find audio type %s".formatted(argument), RED),
      ERR_INVALID_DITHER_TYPE = (algorithm) -> text(
          "Could not find dither type %s".formatted(algorithm), RED),
      ERR_INVALID_VIDEO_TYPE = (mode) -> text("Could not find video mode %s".formatted(mode), RED),
      ERR_CANNOT_CHECK_UPDATES = (msg) -> text("Cannot look for updates: %s".formatted(msg), RED);

  UniComponent<Sender, List<String>> LIST_FFMPEG_ARGS = (list) -> join(
      separator(space()),
      text("Current FFmpeg arguments:", GOLD),
      text(list.toString(), AQUA));

  UniComponent<Sender, Integer>
      PURGE_MAP = (id) ->
      join(
          separator(space()),
          text("Successfully purged all maps with id", GOLD),
          text(id, AQUA)),
      GIVE_MAP_ID = (id) -> join(
          separator(space()), text("Gave map with id", GOLD), text(id, AQUA)),
      CHANGED_VIDEO_MAP_ID = (id) -> join(
          separator(space()),
          text("Set starting map id to", GOLD),
          text(id, AQUA));

  UniComponent<Sender, Long> RESUMING_VIDEO_MS = (ms) -> text("Resuming Video at %s Milliseconds!");

  UniComponent<Sender, Player> SEND_RESOURCEPACK_URL = (player) -> text()
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
      .build();

  BiComponent<Sender, String, byte[]>
      FIN_RESOURCEPACK_INIT = (url, hash) -> text(
      "Loaded Resourcepack Successfully! (URL: %s, Hash: %s)".formatted(url, new String(hash))),
      SENT_RESOURCEPACK = (url, hash) -> text(
          "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash)));

  BiComponent<Sender, String, Integer>
      ADD_FFMPEG_ARG_INDX = (str, index) ->
      join(
          separator(space()),
          text("Added arguments", GOLD),
          text(str, AQUA),
          text("  the FFmpeg command at index", GOLD),
          text(index, AQUA)),
      REMOVE_FFMPEG_ARG_INDX = (str, index) ->
          join(
              separator(space()),
              text("Removed arguments", GOLD),
              text(str, AQUA),
              text("from the FFmpeg command at index", GOLD),
              text(index, AQUA));

  BiComponent<Sender, Integer, Integer>
      CHANGED_IMG_DIMS = (width, height) -> text(
      "Changed itemframe dimensions to %d:%d (width:height)".formatted(width, height)),
      GAVE_MAP_RANGE = (start, end) -> join(
          separator(space()),
          text("Gave maps between IDs", GOLD),
          text(start, AQUA),
          text("and", GOLD),
          text(end, AQUA)),
      CHANGED_VIDEO_SCREEN_DIMS = (width, height) -> join(
          separator(space()),
          text("Set screen dimensions to", GOLD),
          text("%d:%d".formatted(width, height), AQUA),
          text("(width:height)", GOLD)),
      CHANGED_ITEMFRAME_DIMS = (width, height) -> join(
          separator(space()),
          text("Set itemframe map dimensions to", GOLD),
          text("%d:%d".formatted(width, height), AQUA),
          text("(width:height)", GOLD));

  static <T extends Enum<T>> @NotNull Component getComponent(@NotNull final Class<T> clazz,
      @NotNull final Function<T, Component> function) {
    final T[] arr = clazz.getEnumConstants();
    final TextComponent.Builder component = text();
    for (final T value : arr) {
      component.append(function.apply(value));
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
