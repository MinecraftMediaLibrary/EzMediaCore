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
package io.github.pulsebeat02.ezmediacore;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.analysis.Diagnostic;
import io.github.pulsebeat02.ezmediacore.analysis.SystemDiagnostics;
import io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil;
import io.github.pulsebeat02.ezmediacore.listener.RegistrationListener;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyClient;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyProvider;
import io.github.pulsebeat02.ezmediacore.reflect.NMSReflectionHandler;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import io.github.pulsebeat02.ezmediacore.utility.search.StringSearch;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EzMediaCore implements MediaLibraryCore {

  private final SpotifyClient spotifyClient;
  private final Plugin plugin;
  private final Path libraryPath;
  private final Path httpServerPath;
  private final Path dependencyPath;
  private final Path imagePath;
  private final Path audioPath;
  private final Path videoPath;

  private LibraryLoader loader;
  private Diagnostic diagnostics;
  private PacketHandler handler;
  private CoreLogger logger;
  private Listener registrationListener;
  private Path ffmpegExecutable;
  private Path rtpExecutable;
  private Path vlcPath;
  private boolean vlcSupported;
  private boolean disabled;

  EzMediaCore(
      @NotNull final Plugin plugin,
      @Nullable final LibraryLoader loader,
      @Nullable final Path libraryPath,
      @Nullable final Path dependencyPath,
      @Nullable final Path httpServerPath,
      @Nullable final Path imagePath,
      @Nullable final Path audioPath,
      @Nullable final Path videoPath,
      @Nullable final SpotifyClient client) {

    checkNotNull(plugin, "Cannot initialize plugin that is null!");

    this.plugin = plugin;
    this.spotifyClient = client;
    this.loader = loader;

    this.libraryPath = this.getFinalPath(libraryPath, plugin.getDataFolder().toPath(), "emc");
    this.dependencyPath = this.getFinalPath(dependencyPath, this.libraryPath, "libs");
    this.httpServerPath = this.getFinalPath(httpServerPath, this.libraryPath, "http");
    this.imagePath = this.getFinalPath(imagePath, this.libraryPath, "image");
    this.audioPath = this.getFinalPath(audioPath, this.libraryPath, "audio");
    this.videoPath = this.getFinalPath(videoPath, this.libraryPath, "video");

    this.initLogger();
    this.initDiagnostics();
    this.initPacketHandler();
    this.initProviders();
    this.initStream();
    this.initDependencyLoader();
  }

  private @NotNull Path getFinalPath(
      @Nullable final Path path, @NotNull final Path folder, @NotNull final String name) {
    return (path == null ? folder.resolve(name) : path).toAbsolutePath();
  }

  private void initLogger() {
    try {
      this.logger = new ManualLogger(this);
      this.logger.start();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void initDiagnostics() {
    this.diagnostics = new SystemDiagnostics(this);
    this.diagnostics.debugInformation();
    this.logger.info(Locale.SYSTEM_DIAGNOSTIC.build());
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
    this.handler = new NMSReflectionHandler(this).getNewPacketHandlerInstance();
  }

  private void initProviders() {
    DitherLookupUtil.init();
    StringSearch.init();
    SpotifyProvider.init(this);
    this.logger.info(Locale.LOOKUP_CACHE.build());
  }

  private void initStream() {
    IntStream.range(0, 50).parallel().forEach(key -> {}); // jump start int stream
  }

  private void initDependencyLoader() {
    this.loader = (this.loader == null ? new DependencyLoader(this) : this.loader);
  }

  @Override
  public void initialize() {
    this.registerEvents();
    this.createFolders();
    this.startDependencyLoader();
    this.sendUsageTips();
  }

  private void registerEvents() {
    this.registrationListener = new RegistrationListener(this);
    this.logger.info(Locale.EVENT_REGISTRATION.build());
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
    this.logger.info(Locale.FILE_CREATION.build());
  }

  private void createFiles() {
    FileUtils.copyFromResourcesExceptionally("audio.html", this.httpServerPath.resolve("audio.html"));
  }

  private void startDependencyLoader() {
    this.loader.start();
    this.logger.info(Locale.DEPENDENCY_LOADING.build());
  }

  private void sendUsageTips() {
    this.sendWarningMessage();
    this.sendPacketCompressionTip();
    this.sendSpotifyWarningMessage(this);
  }

  private void sendWarningMessage() {
    this.logger.warn(Locale.SERVER_SOFTWARE.build());
  }

  private void sendPacketCompressionTip() {
    if (this.isOnlineMode()) {
      this.logger.warn(Locale.PACKET_COMPRESSION.build());
    }
  }

  private boolean isOnlineMode() {
    return this.plugin.getServer().getOnlineMode();
  }

  private void sendSpotifyWarningMessage(@NotNull final MediaLibraryCore core) {
    if (this.invalidSpotifyClient(core)) {
      this.logger.warn(Locale.SPOTIFY_AUTHENTICATION.build());
    }
  }

  private boolean invalidSpotifyClient(@NotNull final MediaLibraryCore core) {
    return core.getSpotifyClient() == null;
  }

  @Override
  public void shutdown() {
    this.disabled = true;
    HandlerList.unregisterAll(this.registrationListener);
    this.closeLogger();
  }

  private void closeLogger() {
    Try.closeable(this.logger);
  }

  @Override
  public @NotNull Plugin getPlugin() {
    return this.plugin;
  }

  @Override
  public @NotNull PacketHandler getHandler() {
    return this.handler;
  }

  @Override
  public @NotNull Path getLibraryPath() {
    return this.libraryPath;
  }

  @Override
  public @NotNull Path getHttpServerPath() {
    return this.httpServerPath;
  }

  @Override
  public @NotNull Path getDependencyPath() {
    return this.dependencyPath;
  }

  @Override
  public @NotNull Path getImagePath() {
    return this.imagePath;
  }

  @Override
  public @NotNull Path getAudioPath() {
    return this.audioPath;
  }

  @Override
  public @NotNull Path getVideoPath() {
    return this.videoPath;
  }

  @Override
  public @NotNull Path getFFmpegPath() {
    return this.ffmpegExecutable;
  }

  @Override
  public void setFFmpegPath(@NotNull final Path path) {
    this.ffmpegExecutable = path.toAbsolutePath();
  }

  @Override
  public @NotNull Path getRTPPath() {
    return this.rtpExecutable;
  }

  @Override
  public void setRTPPath(@NotNull final Path path) {
    this.rtpExecutable = path.toAbsolutePath();
  }

  @Override
  public @NotNull Path getVlcPath() {
    return this.vlcPath;
  }

  @Override
  public void setVlcPath(@NotNull final Path path) {
    this.vlcPath = path;
  }

  @Override
  public boolean isDisabled() {
    return this.disabled;
  }

  @Override
  public @NotNull Listener getRegistrationHandler() {
    return this.registrationListener;
  }

  @Override
  public void setRegistrationHandler(@NotNull final Listener listener) {
    this.registrationListener = listener;
  }

  @Override
  public @NotNull LibraryLoader getLibraryLoader() {
    return this.loader;
  }

  @Override
  public @NotNull CoreLogger getLogger() {
    return this.logger;
  }

  @Override
  public @Nullable SpotifyClient getSpotifyClient() {
    return this.spotifyClient;
  }

  @Override
  public boolean isVLCSupported() {
    return this.vlcSupported;
  }

  @Override
  public void setVLCStatus(final boolean vlcStatus) {
    this.vlcSupported = vlcStatus;
  }

  @Override
  public @NotNull Diagnostic getDiagnostics() {
    return this.diagnostics;
  }
}
