/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
import org.bukkit.entity.Player;

import java.util.List;

import static io.github.pulsebeat02.deluxemediaplugin.locale.LocaleParent.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;


public interface Locale extends LocaleParent{

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
  UniComponent<Sender, String> SET_MEDIA = info("deluxemediaplugin.command.video.load.type", null);
  UniComponent<Sender, String> HTTP_SEND_LINK = mrl ->
          format(translatable("deluxemediaplugin.command.video.resourcepack.link.0", GOLD)
                  .append(translatable("deluxemediaplugin.command.video.resourcepack.link.1")
                          .style(style(AQUA, BOLD, UNDERLINED, openUrl(mrl)))));

  UniComponent<Sender, String> PLAY_VIDEO = info("deluxemediaplugin.command.video.play", null);
  UniComponent<Sender, String> LOADED_MEDIA = info("deluxemediaplugin.command.video.load.success", null);
  UniComponent<Sender, String> SET_AUDIO_PLAYBACK = info("deluxemediaplugin.command.video.set.audio-playback", null);
  UniComponent<Sender, String> SET_DITHER_ALGORITHM = info("deluxemediaplugin.command.video.set.dither-algorithm", null);
  UniComponent<Sender, String> SET_PLAYER_ALGORITHM = info("deluxemediaplugin.command.video.set.player-algorithm", null);
  UniComponent<Sender, String> SET_VIDEO_PLAYBACK = info("deluxemediaplugin.command.video.set.video-playback", null);
  UniComponent<Sender, String> NEW_UPDATE_PLUGIN = info("deluxemediaplugin.log.update.available", null);
  UniComponent<Sender, String> INVALID_AUDIO_PLAYBACK = error("deluxemediaplugin.command.video.set.audio-playback.error", null);
  UniComponent<Sender, String> INVALID_DITHER_ALGORITHM = error("deluxemediaplugin.command.video.set.dither-algorithm.error", null);
  UniComponent<Sender, String> INVALID_VIDEO_PLAYBACK = error("deluxemediaplugin.command.video.set.video-playback.error", null);
  UniComponent<Sender, String> CANNOT_CHECK_UPDATES = error("deluxemediaplugin.log.update.error", null);
  UniComponent<Sender, String> INVALID_PLAYER_ALGORITHM = error("deluxemediaplugin.command.video.set.player-algorithm.error", null);
  UniComponent<Sender, DitheringAlgorithm> INVALID_NATIVE_DITHERING_ALGORITHM = error("deluxemediaplugin.command.video.set.native-dithering.error", null);
  UniComponent<Sender, Integer> GIVE_SINGLE_MAP = info("deluxemediaplugin.command.map", null);
  UniComponent<Sender, Integer> CHANGED_VIDEO_MAP_ID = info("deluxemediaplugin.command.video.set.starting-map", null);
  UniComponent<Sender, String> SEND_RESOURCEPACK_URL = player ->
          format(translatable("deluxemediaplugin.command.video.resourcepack.result.0", GOLD, List.of(translatable("deluxemediaplugin.command.video.resourcepack.result.1",
                  style(AQUA, BOLD, UNDERLINED, runCommand("/video resourcepack load %s".formatted(player)))))));
  BiComponent<Sender, String, byte[]> RESOURCEPACK_INFO = info("deluxemediaplugin.command.video.resourcepack.sent", null, String::new);
  BiComponent<Sender, DitheringAlgorithm, Boolean> SET_NATIVE_DITHERING = info("deluxemediaplugin.command.video.set.native-dithering", null,  enabled -> enabled ? "on" : "off");
  BiComponent<Sender, Integer, Integer> GIVE_MAP_RANGE = info("deluxemediaplugin.command.map.range", null, null);
  BiComponent<Sender, Integer, Integer> SET_RESOLUTION = info("deluxemediaplugin.command.video.set.resolution", null, null);
  BiComponent<Sender, Integer, Integer> SET_ITEMFRAME_DIMENSION = info("deluxemediaplugin.command.video.set.itemframe-dimension", null, null);
  BiComponent<Sender, String, String> INVALID_PLAYER_MEDIA = error("deluxemediaplugin.command.video.load.player.invalid-input", null, null);
}
