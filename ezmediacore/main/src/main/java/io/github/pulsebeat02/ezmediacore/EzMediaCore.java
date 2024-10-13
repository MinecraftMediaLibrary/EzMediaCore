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
package io.github.pulsebeat02.ezmediacore;

import static com.google.common.base.Preconditions.checkNotNull;

import rewrite.dependency.DependencyLoader;
import rewrite.dither.load.ColorPalette;
import rewrite.listener.RegistrationListener;
import rewrite.locale.Locale;
import rewrite.logging.LibraryLogger;
import rewrite.logging.Logger;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import rewrite.reflect.PacketToolsProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class EzMediaCore {

  /*

  Play videos with shaders on https://hangar.papermc.io/JNNGL/VanillaMinimaps

   */

  private final Plugin plugin;

  private final Path libraryPath;
  private final Path httpServerPath;
  private final Path dependencyPath;
  private final Path imagePath;
  private final Path audioPath;
  private final Path videoPath;

  private DependencyLoader loader;
  private PacketHandler handler;
  private Logger logger;
  private Listener registrationListener;
  private boolean disabled;

  EzMediaCore(
       final Plugin plugin,
       final DependencyLoader loader,
       final Path libraryPath,
       final Path dependencyPath,
       final Path httpServerPath,
       final Path imagePath,
       final Path audioPath,
       final Path videoPath) {

    checkNotNull(plugin, "Cannot initialize plugin that is null!");

    this.plugin = plugin;
    this.loader = loader;

    this.libraryPath = this.getFinalPath(libraryPath, plugin.getDataFolder().toPath(), "emc");
    this.dependencyPath = this.getFinalPath(dependencyPath, this.libraryPath, "libs");
    this.httpServerPath = this.getFinalPath(httpServerPath, this.libraryPath, "http");
    this.imagePath = this.getFinalPath(imagePath, this.libraryPath, "image");
    this.audioPath = this.getFinalPath(audioPath, this.libraryPath, "audio");
    this.videoPath = this.getFinalPath(videoPath, this.libraryPath, "video");

    this.initLogger();
    this.initPacketHandler();
    this.initProviders();
    this.initStream();
    this.initDependencyLoader();
  }

  private  Path getFinalPath(
       final Path path,  final Path folder,  final String name) {
    return (path == null ? folder.resolve(name) : path).toAbsolutePath();
  }

  private void initLogger() {
    try {
      this.logger = new LibraryLogger(this);
      this.logger.start();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void initPacketHandler() {
    if (!this.isMocking()) {
      this.assignPacketHandler();
      this.logger.info(Locale.PACKET_HANDLER.build());
    }
  }

  private boolean isMocking() {
    return this.plugin.getServer().getVersion().contains("MockBukkit");
  }

  private void assignPacketHandler() {
    this.handler = new PacketToolsProvider(this).getNewPacketHandlerInstance();
  }

  private void initProviders() {
    ColorPalette.init();
    this.logger.info(Locale.LOOKUP_CACHE.build());
  }

  private void initStream() {
    IntStream.range(0, 50).parallel().forEach(key -> {}); // jump start int stream
  }

  private void initDependencyLoader() {
    this.loader = (this.loader == null ? new DependencyLoader(this) : this.loader);
  }

  public void initialize() {
    this.registerEvents();
    this.createResources();
    this.startDependencyLoader();
    this.sendUsageTips();
  }

  private void registerEvents() {
    this.registrationListener = new RegistrationListener(this);
    this.logger.info(Locale.EVENT_REGISTRATION.build());
  }

  private void createResources() {
    this.createFolders();
    this.logger.info(Locale.FILE_CREATION.build());
  }

  private void createFolders() {
    Stream.of(
            this.libraryPath,
            this.dependencyPath,
            this.httpServerPath,
            this.imagePath,
            this.audioPath,
            this.videoPath)
        .forEach(FileUtils::createDirectoryIfNotExistsExceptionally);
  }

  private void startDependencyLoader() {
    this.loader.start();
    this.logger.info(Locale.DEPENDENCY_LOADING.build());
  }

  private void sendUsageTips() {
    this.sendWarningMessage();
    this.sendSpotifyWarningMessage(this);
  }

  private void sendWarningMessage() {
    this.logger.warn(Locale.SERVER_SOFTWARE.build());
  }

  private boolean isOnlineMode() {
    return this.plugin.getServer().getOnlineMode();
  }

  private void sendSpotifyWarningMessage( final EzMediaCore core) {
    if (this.invalidSpotifyClient(core)) {
      this.logger.warn(Locale.SPOTIFY_AUTHENTICATION.build());
    }
  }

  private boolean invalidSpotifyClient( final EzMediaCore core) {
    return core.getSpotifyClient() == null;
  }

  public void shutdown() {
    this.disabled = true;
    HandlerList.unregisterAll(this.registrationListener);
    this.closeLogger();
  }

  private void closeLogger() {
    Try.closeable(this.logger);
  }

  public  Plugin getPlugin() {
    return this.plugin;
  }

  public  PacketHandler getHandler() {
    return this.handler;
  }

  public  Path getLibraryPath() {
    return this.libraryPath;
  }

  public  Path getHttpServerPath() {
    return this.httpServerPath;
  }

  public  Path getDependencyPath() {
    return this.dependencyPath;
  }

  public  Path getImagePath() {
    return this.imagePath;
  }

  public  Path getAudioPath() {
    return this.audioPath;
  }

  public  Path getVideoPath() {
    return this.videoPath;
  }

  public boolean isDisabled() {
    return this.disabled;
  }

  public Listener getRegistrationHandler() {
    return this.registrationListener;
  }

  public void setRegistrationHandler( final Listener listener) {
    this.registrationListener = listener;
  }

  public DependencyLoader getLibraryLoader() {
    return this.loader;
  }

  public Logger getLogger() {
    return this.logger;
  }
}
