package io.github.pulsebeat02.epicmedialib;

import io.github.pulsebeat02.epicmedialib.analysis.Diagnostic;
import io.github.pulsebeat02.epicmedialib.analysis.SystemDiagnostics;
import io.github.pulsebeat02.epicmedialib.listener.RegistrationListener;
import io.github.pulsebeat02.epicmedialib.nms.PacketHandler;
import io.github.pulsebeat02.epicmedialib.reflect.NMSReflectionHandler;
import io.github.pulsebeat02.epicmedialib.reflect.TinyProtocol;
import io.github.pulsebeat02.epicmedialib.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.epicmedialib.throwable.LibraryException;
import io.github.pulsebeat02.epicmedialib.utility.PluginUsageTips;
import io.netty.channel.Channel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EpicMediaLib implements MediaLibraryCore {

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
  private Listener registrationListener;
  private Path ffmpegExecutable;
  private boolean disabled;

  EpicMediaLib(@NotNull final Plugin plugin) {
    this(plugin, null, null, null, null, null, null, null, null);
  }

  EpicMediaLib(
      @NotNull final Plugin plugin,
      @Nullable final LibraryLoader loader,
      @Nullable final Path libraryPath,
      @Nullable final Path dependencyPath,
      @Nullable final Path httpServerPath,
      @Nullable final Path vlcPath,
      @Nullable final Path imagePath,
      @Nullable final Path audioPath,
      @Nullable final Path videoPath) {
    this.plugin = plugin;
    this.libraryPath =
        libraryPath == null ? plugin.getDataFolder().toPath().resolve("mml") : libraryPath;
    this.dependencyPath = dependencyPath == null ? libraryPath.resolve("libs") : dependencyPath;
    this.httpServerPath = httpServerPath == null ? libraryPath.resolve("http") : httpServerPath;
    this.vlcPath = vlcPath == null ? dependencyPath.resolve("vlc") : vlcPath;
    this.imagePath = imagePath == null ? libraryPath.resolve("image") : imagePath;
    this.audioPath = audioPath == null ? libraryPath.resolve("audio") : audioPath;
    this.videoPath = videoPath == null ? libraryPath.resolve("video") : videoPath;
    this.diagnostics = new SystemDiagnostics(this);
    this.loader = loader == null ? new DependencyLoader(this) : loader;
  }

  @Override
  public void initialize() throws ExecutionException, InterruptedException {

    Logger.init(this);

    Stream.of(
            this.libraryPath,
            this.dependencyPath,
            this.httpServerPath,
            this.vlcPath,
            this.imagePath,
            this.audioPath,
            this.videoPath)
        .forEach(
            ThrowingConsumer.unchecked(
                Files::createDirectories,
                this.plugin.getLogger(),
                Level.SEVERE,
                "[EpicMediaLib]: A severe I/O exception occurred while trying to create library folders!"));

    this.registrationListener = new RegistrationListener(this);

    final Optional<PacketHandler> optional = NMSReflectionHandler.getNewPacketHandlerInstance();
    if (optional.isPresent()) {
      this.handler = optional.orElseThrow(AssertionError::new);
      new TinyProtocol(this.plugin) {
        @Override
        public Object onPacketOutAsync(
            final Player player, final Channel channel, final Object packet) {
          return EpicMediaLib.this.handler.onPacketInterceptOut(player, packet);
        }

        @Override
        public Object onPacketInAsync(
            final Player player, final Channel channel, final Object packet) {
          return EpicMediaLib.this.handler.onPacketInterceptIn(player, packet);
        }
      };
    } else {
      throw new LibraryException("Unsupported library version! Only supports 1.16.5 - 1.17!");
    }

    this.loader.start();

    PluginUsageTips.sendWarningMessage();
    PluginUsageTips.sendPacketCompressionTip();
  }

  @Override
  public void shutdown() {
    Logger.info("Shutting Down");
    this.disabled = true;
    HandlerList.unregisterAll(this.registrationListener);
    Logger.info("Good Bye! :(");
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
    this.ffmpegExecutable = path;
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
  public @NotNull Diagnostic getDiagnostics() {
    return this.diagnostics;
  }
}
