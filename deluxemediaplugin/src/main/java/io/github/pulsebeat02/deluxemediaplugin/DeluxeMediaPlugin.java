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
import io.github.pulsebeat02.deluxemediaplugin.executors.ExecutorProvider;
import io.github.pulsebeat02.deluxemediaplugin.json.MediaAttributesData;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeluxeMediaPlugin {

  private final JavaPlugin plugin;

  private AudioConfiguration audioConfiguration;
  private MediaLibraryCore library;
  private BukkitAudiences audiences;
  private Audience console;
  private CommandHandler handler;
  private PersistentPictureManager manager;
  private HttpServer server;
  private MediaBot mediaBot;
  private ServerInfo httpAudioServer;
  private VideoCommandAttributes attributes;
  private MediaAttributesData mediaAttributesData;

  public DeluxeMediaPlugin(@NotNull final JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void enable() {
    this.startLibrary();
    this.loadPersistentData();
    this.registerCommands();
    this.startMetrics();
    this.checkUpdates();
    this.finishEnabling();
  }

  public void disable() throws Exception {
    this.shutdownLibrary();
    this.deserializeData();
    this.unregisterCommands();
    this.disableBot();
    this.cancelNativeTasks();
    this.finishDisabling();
  }

  public void load() {
    this.audiences = BukkitAudiences.create(this.plugin);
    this.console = this.audiences.console();
    this.finishLoading();
  }

  private void deserializeData() {
    try {
      this.mediaAttributesData.deserialize(this.attributes);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.console.sendMessage(Locale.DESERIALIZE_DATA.build());
  }

  private void startMetrics() {
    new Metrics(this.plugin, 10229);
    this.console.sendMessage(Locale.FIN_METRICS_INIT.build());
  }

  private void finishLoading() {}

  private void finishEnabling() {
    this.checkUpdates();
    this.console.sendMessage(Locale.FIN_PLUGIN_INIT.build());
    this.console.sendMessage(Locale.WELCOME.build());
  }

  private void finishDisabling() {
    this.console.sendMessage(Locale.GOODBYE.build());
  }

  private void startLibrary() {
    this.console.sendMessage(Locale.PLUGIN_LOGO.build());
    this.console.sendMessage(Locale.ENABLE_PLUGIN.build());
    this.console.sendMessage(Locale.EMC_INIT.build());
    try {
      this.library = LibraryProvider.builder().plugin(this.plugin).build();
      this.library.initialize();
    } catch (final ExecutionException | InterruptedException e) {
      this.console.sendMessage(Locale.ERR_EMC_INIT.build());
      e.printStackTrace();
      this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }
    this.console.sendMessage(Locale.FIN_EMC_INIT.build());
  }

  private void checkUpdates() {
    new UpdateChecker(this).check();
  }

  private void disableBot() {
    if (this.mediaBot != null) {
      this.mediaBot.getJDA().shutdown();
    }
    this.console.sendMessage(Locale.DISABLE_BOT.build());
  }

  private void unregisterCommands() {
    if (this.handler != null) {
      for (final BaseCommand cmd : this.handler.getCommands()) {
        CommandUtils.unRegisterBukkitCommand(this, cmd);
      }
    }
    this.console.sendMessage(Locale.DISABLE_COMMANDS.build());
  }

  private void shutdownLibrary() {
    this.console.sendMessage(Locale.DISABLE_PLUGIN.build());
    if (this.library != null) {
      this.library.shutdown();
      this.console.sendMessage(Locale.DISABLE_EMC.build());
    } else {
      this.console.sendMessage(Locale.ERR_EMC_SHUTDOWN.build());
    }
  }

  private void cancelNativeTasks() throws Exception {
    this.cancelNativeExtractor();
    this.cancelNativeStreamExtractor();
    this.console.sendMessage(Locale.CANCELLED_TASKS.build());
  }

  private void cancelNativeExtractor() throws Exception {
    final EnhancedExecution extractor = this.attributes.getExtractor();
    if (extractor != null) {
      extractor.close();
    }
  }

  private void cancelNativeStreamExtractor() throws Exception {
    final EnhancedExecution streamExtractor = this.attributes.getStreamExtractor();
    if (streamExtractor != null) {
      streamExtractor.close();
    }
  }

  private void createFolders() {
    Set.of(this.plugin.getDataFolder().toPath().resolve("configuration"))
        .forEach(ThrowingConsumer.unchecked(FileUtils::createFolderIfNotExists));
  }

  private void readConfigurationFiles() throws IOException {
    this.readHttpConfiguration();
    this.readEncoderConfiguration();
    this.readBotConfiguration();
    this.readStreamAudioConfiguration();
    this.readPictureData();
  }

  private void readHttpConfiguration() throws IOException {
    final HttpConfiguration httpConfiguration = new HttpConfiguration(this);
    httpConfiguration.read();
    this.server = httpConfiguration.getSerializedValue();
  }

  private void readEncoderConfiguration() throws IOException {
    final EncoderConfiguration encoderConfiguration = new EncoderConfiguration(this);
    encoderConfiguration.read();
    this.audioConfiguration = encoderConfiguration.getSerializedValue();
  }

  private void readBotConfiguration() throws IOException {
    final BotConfiguration botConfiguration = new BotConfiguration(this);
    botConfiguration.read();
    this.mediaBot = botConfiguration.getSerializedValue();
  }

  private void readStreamAudioConfiguration() throws IOException {
    final HttpAudioConfiguration audioConfiguration = new HttpAudioConfiguration(this);
    audioConfiguration.read();
    this.httpAudioServer = audioConfiguration.getSerializedValue();
  }

  private void readJsonFiles() throws IOException {
    this.mediaAttributesData = new MediaAttributesData(this);
    this.mediaAttributesData.serialize();
    this.attributes = this.mediaAttributesData.getSerializedValue();
  }

  private void readPictureData() throws IOException {
    this.manager = new PersistentPictureManager(this);
    this.manager.startTask();
  }

  private void loadPersistentData() {
    try {
      this.createFolders();
      this.readConfigurationFiles();
      this.readJsonFiles();
      this.writeToFile();
    } catch (final IOException e) {
      this.console.sendMessage(Locale.ERR_PERSISTENT_INIT.build());
      e.printStackTrace();
    }
    this.console.sendMessage(Locale.FIN_PERSISTENT_INIT.build());
  }

  private void writeToFile() {
    ExecutorProvider.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
        this::deserializeAttributes, 0L, 5L, TimeUnit.MINUTES);
  }

  private void deserializeAttributes() {
    try {
      this.mediaAttributesData.deserialize(this.attributes);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void registerCommands() {
    this.handler = new CommandHandler(this);
    this.console.sendMessage(Locale.FIN_COMMANDS_INIT.build());
  }

  public Audience getLogger() {
    return this.console;
  }

  public @NotNull JavaPlugin getBootstrap() {
    return this.plugin;
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

  public @Nullable ServerInfo getHttpAudioServer() {
    return this.httpAudioServer;
  }
}
