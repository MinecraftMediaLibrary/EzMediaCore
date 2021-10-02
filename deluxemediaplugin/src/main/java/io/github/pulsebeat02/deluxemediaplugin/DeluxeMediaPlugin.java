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

package io.github.pulsebeat02.deluxemediaplugin;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;

import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.config.BotConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpAudioConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.PersistentPictureManager;
import io.github.pulsebeat02.deluxemediaplugin.config.ServerInfo;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.update.UpdateChecker;
import io.github.pulsebeat02.deluxemediaplugin.utility.CommandUtils;
import io.github.pulsebeat02.ezmediacore.LibraryProvider;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeluxeMediaPlugin {

  private final JavaPlugin plugin;

  private MediaLibraryCore library;
  private BukkitAudiences audiences;
  private Audience console;
  private CommandHandler handler;

  private AudioConfiguration audioConfiguration;
  private PersistentPictureManager manager;
  private HttpServer server;
  private MediaBot mediaBot;
  private ServerInfo httpAudioServer;

  private VideoCommandAttributes attributes;

  public DeluxeMediaPlugin(@NotNull final JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void enable() {
    this.audiences = BukkitAudiences.create(this.plugin);
    this.console = this.audiences.console();
    this.printLogo();
    this.console.sendMessage(Locale.ENABLE_PLUGIN.build());
    this.console.sendMessage(Locale.EMC_INIT.build());
    this.startLibrary();
    this.console.sendMessage(Locale.FIN_EMC_INIT.build());
    this.loadPersistentData();
    this.console.sendMessage(Locale.FIN_PERSISTENT_INIT.build());
    this.registerCommands();
    this.console.sendMessage(Locale.FIN_COMMANDS_INIT.build());
    this.startMetrics();
    this.console.sendMessage(Locale.FIN_METRICS_INIT.build());
    this.checkUpdates();
    this.console.sendMessage(Locale.FIN_PLUGIN_INIT.build());
    this.console.sendMessage(Locale.WELCOME.build());
  }

  public void disable() throws Exception {
    this.console.sendMessage(Locale.DISABLE_PLUGIN.build());
    this.shutdownLibrary();
    this.unregisterCommands();
    this.disableBot();
    this.cancelNativeTasks();
    this.console.sendMessage(Locale.GOODBYE.build());
  }

  public void load() {}

  private void startLibrary() {
    try {
      this.library = LibraryProvider.builder().plugin(this.plugin).build();
      this.library.initialize();
    } catch (final ExecutionException | InterruptedException e) {
      this.console.sendMessage(Locale.ERR_EMC_INIT.build());
      e.printStackTrace();
      this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }
  }

  private void startMetrics() {
    new Metrics(this.plugin, 10229);
  }

  private void checkUpdates() {
    new UpdateChecker(this).check();
  }

  private void printLogo() {
    final List<String> logo =
        Arrays.asList(
            " _____       _                __  __          _ _       _____  _             _       ",
            " |  __ \\     | |              |  \\/  |        | (_)     |  __ \\| |           (_)      ",
            " | |  | | ___| |_   ___  _____| \\  / | ___  __| |_  __ _| |__) | |_   _  __ _ _ _ __  ",
            " | |  | |/ _ \\ | | | \\ \\/ / _ \\ |\\/| |/ _ \\/ _` | |/ _` |  ___/| | | | |/ _` | | '_ \\ ",
            " | |__| |  __/ | |_| |>  <  __/ |  | |  __/ (_| | | (_| | |    | | |_| | (_| | | | | |",
            " |_____/ \\___|_|\\__,_/_/\\_\\___|_|  |_|\\___|\\__,_|_|\\__,_|_|    |_|\\__,_|\\__, |_|_| |_|",
            "                                                                         __/ |        ",
            "                                                                        |___/         ");
    for (final String line : logo) {
      this.audiences.console().sendMessage(text(line, BLUE));
    }
  }

  private void disableBot() {
    if (this.mediaBot != null) {
      this.mediaBot.getJDA().shutdown();
    }
  }

  private void unregisterCommands() {
    if (this.handler != null) {
      for (final BaseCommand cmd : this.handler.getCommands()) {
        CommandUtils.unRegisterBukkitCommand(this, cmd);
      }
    }
  }

  private void shutdownLibrary() {
    if (this.library != null) {
      this.library.shutdown();
      this.console.sendMessage(Locale.GOOD_EMC_SHUTDOWN.build());
    } else {
      this.console.sendMessage(Locale.ERR_EMC_SHUTDOWN.build());
    }
  }

  private void cancelNativeTasks() throws Exception {
    final EnhancedExecution extractor = this.attributes.getExtractor();
    if (extractor != null) {
      extractor.close();
    }
    final EnhancedExecution streamExtractor = this.attributes.getStreamExtractor();
    if (streamExtractor != null) {
      streamExtractor.close();
    }
  }

  private void loadPersistentData() {
    try {
      Set.of(this.plugin.getDataFolder().toPath().resolve("configuration"))
          .forEach(ThrowingConsumer.unchecked(FileUtils::createFolderIfNotExists));
      final HttpConfiguration httpConfiguration = new HttpConfiguration(this);
      final EncoderConfiguration encoderConfiguration = new EncoderConfiguration(this);
      final BotConfiguration botConfiguration = new BotConfiguration(this);
      final HttpAudioConfiguration audioConfiguration = new HttpAudioConfiguration(this);
      httpConfiguration.read();
      encoderConfiguration.read();
      botConfiguration.read();
      audioConfiguration.read();
      this.server = httpConfiguration.getSerializedValue();
      this.audioConfiguration = encoderConfiguration.getSerializedValue();
      this.mediaBot = botConfiguration.getSerializedValue();
      this.httpAudioServer = audioConfiguration.getSerializedValue();
      this.manager = new PersistentPictureManager(this);
      this.manager.startTask();
    } catch (final IOException e) {
      this.console.sendMessage(Locale.ERR_PERSISTENT_INIT.build());
      e.printStackTrace();
    }
  }

  public void log(@NotNull final String line) {
    this.log(format(text(line)));
  }

  public void log(@NotNull final Component line) {
    this.audiences.console().sendMessage(line);
  }

  public @NotNull JavaPlugin getBootstrap() {
    return this.plugin;
  }

  private void registerCommands() {
    this.handler = new CommandHandler(this);
  }

  public @NotNull PersistentPictureManager getPictureManager() {
    return this.manager;
  }

  public @NotNull AudioConfiguration getAudioConfiguration() {
    return this.audioConfiguration;
  }

  public @NotNull HttpServer getHttpServer() {
    return this.server;
  }

  public @Nullable MediaBot getMediaBot() {
    return this.mediaBot;
  }

  public @NotNull MediaLibraryCore library() {
    return this.library;
  }

  public @NotNull BukkitAudiences audience() {
    return this.audiences;
  }

  public @NotNull VideoCommandAttributes getAttributes() {
    return this.attributes;
  }

  public void setAttributes(final VideoCommandAttributes attributes) {
    this.attributes = attributes;
  }

  public @Nullable ServerInfo getHttpAudioServer() {
    return this.httpAudioServer;
  }
}
