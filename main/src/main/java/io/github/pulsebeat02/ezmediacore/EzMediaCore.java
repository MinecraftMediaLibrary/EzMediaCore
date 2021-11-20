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

import io.github.pulsebeat02.ezmediacore.analysis.Diagnostic;
import io.github.pulsebeat02.ezmediacore.analysis.SystemDiagnostics;
import io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil;
import io.github.pulsebeat02.ezmediacore.listener.RegistrationListener;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyClient;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyProvider;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.YoutubeProvider;
import io.github.pulsebeat02.ezmediacore.reflect.NMSReflectionHandler;
import io.github.pulsebeat02.ezmediacore.throwable.UnsupportedServerException;
import io.github.pulsebeat02.ezmediacore.utility.search.StringSearch;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EzMediaCore implements MediaLibraryCore {

  private final SpotifyClient spotifyClient;
  private final Plugin plugin;
  private final LibraryLoader loader;
  private final Diagnostic diagnostics;
  private final Path libraryPath;
  private final Path httpServerPath;
  private final Path dependencyPath;
  private final Path vlcPath;
  private final Path imagePath;
  private final Path audioPath;
  private final Path videoPath;

  private PacketHandler handler;
  private CoreLogger logger;
  private Listener registrationListener;
  private Path ffmpegExecutable;
  private Path rtpExecutable;
  private boolean vlcSupported;
  private boolean disabled;

  EzMediaCore(
      @NotNull final Plugin plugin,
      @Nullable final LibraryLoader loader,
      @Nullable final Path libraryPath,
      @Nullable final Path dependencyPath,
      @Nullable final Path httpServerPath,
      @Nullable final Path vlcPath,
      @Nullable final Path imagePath,
      @Nullable final Path audioPath,
      @Nullable final Path videoPath,
      @Nullable final SpotifyClient client) {
    this.plugin = plugin;
    this.spotifyClient = client;
    this.libraryPath = this.getFinalPath(libraryPath, plugin.getDataFolder().toPath(), "emc");
    this.dependencyPath = this.getFinalPath(dependencyPath, this.libraryPath, "libs");
    this.httpServerPath = this.getFinalPath(httpServerPath, this.libraryPath, "http");
    this.imagePath = this.getFinalPath(imagePath, this.libraryPath, "image");
    this.audioPath = this.getFinalPath(audioPath, this.libraryPath, "audio");
    this.videoPath = this.getFinalPath(videoPath, this.libraryPath, "video");
    this.vlcPath = this.getFinalPath(vlcPath, this.dependencyPath, "vlc");
    this.initLogger();
    this.diagnostics = new SystemDiagnostics(this);
    this.initPacketHandler();
    this.initializeProviders();
    this.initializeStream();
    this.loader = (loader == null ? new DependencyLoader(this) : loader);
  }

  private void initPacketHandler() {
    final Optional<PacketHandler> handler =
        new NMSReflectionHandler(this).getNewPacketHandlerInstance();
    if (!Bukkit.getVersion().contains("MockBukkit")) { // hack to allow unit testing
      this.handler = handler.orElseThrow(UnsupportedServerException::new);
    }
  }

  private void initLogger() {
    try {
      this.logger = new ManualLogger(this);
      this.logger.start();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private @NotNull Path getFinalPath(
      @Nullable final Path path, @NotNull final Path folder, @NotNull final String name) {
    return (path == null ? folder.resolve(name) : path).toAbsolutePath();
  }

  @Override
  public void initialize() {
    this.registrationListener = new RegistrationListener(this);
    this.createFolders();
    this.loader.start();
    this.sendUsageTips();
  }

  private void createFolders() {
    Set.of(
            this.libraryPath,
            this.dependencyPath,
            this.httpServerPath,
            this.vlcPath,
            this.imagePath,
            this.audioPath,
            this.videoPath)
        .forEach(ThrowingConsumer.unchecked(Files::createDirectories));
  }

  private void initializeStream() {
    IntStream.range(0, 5).parallel().forEach(key -> {}); // jump start int stream
  }

  private void initializeProviders() {
    DitherLookupUtil.init();
    StringSearch.init();
    SpotifyProvider.init(this);
    YoutubeProvider.init(this);
  }

  private void sendUsageTips() {
    this.sendWarningMessage();
    this.sendPacketCompressionTip();
    this.sendSpotifyWarningMessage(this);
  }

  private void sendWarningMessage() {
    this.logger.warn(Locale.SERVER_SOFTWARE_TIP);
  }

  private void sendPacketCompressionTip() {
    if (Bukkit.getOnlineMode()) {
      this.logger.warn(Locale.PACKET_COMPRESSION_TIP);
    }
  }

  private void sendSpotifyWarningMessage(@NotNull final MediaLibraryCore core) {
    if (core.getSpotifyClient() == null) {
      this.logger.warn(Locale.WARN_SPOTIFY_AUTH);
    }
  }

  @Override
  public void shutdown() {
    this.disabled = true;
    HandlerList.unregisterAll(this.registrationListener);
    try {
      this.logger.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
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
  public @NotNull Path getVlcPath() {
    return this.vlcPath;
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
