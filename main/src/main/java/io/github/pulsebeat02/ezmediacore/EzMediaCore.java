package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.analysis.Diagnostic;
import io.github.pulsebeat02.ezmediacore.analysis.SystemDiagnostics;
import io.github.pulsebeat02.ezmediacore.dither.DitherLookupUtil;
import io.github.pulsebeat02.ezmediacore.listener.RegistrationListener;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.reflect.NMSReflectionHandler;
import io.github.pulsebeat02.ezmediacore.reflect.TinyProtocol;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.throwable.LibraryException;
import io.github.pulsebeat02.ezmediacore.utility.PluginUsageTips;
import io.netty.channel.Channel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EzMediaCore implements MediaLibraryCore {

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

  EzMediaCore(@NotNull final Plugin plugin) {
    this(plugin, null, null, null, null, null, null, null, null);
  }

  EzMediaCore(
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
        (libraryPath == null ? plugin.getDataFolder().toPath().resolve("emc") : libraryPath);
    this.dependencyPath =
        (dependencyPath == null ? this.libraryPath.resolve("libs") : dependencyPath);
    this.httpServerPath =
        (httpServerPath == null ? this.libraryPath.resolve("http") : httpServerPath);
    this.vlcPath = (vlcPath == null ? this.dependencyPath.resolve("vlc") : vlcPath);
    this.imagePath = (imagePath == null ? this.libraryPath.resolve("image") : imagePath);
    this.audioPath = (audioPath == null ? this.libraryPath.resolve("audio") : audioPath);
    this.videoPath = (videoPath == null ? this.libraryPath.resolve("video") : videoPath);

    Logger.init(this);

    this.diagnostics = new SystemDiagnostics(this);
    this.loader = (loader == null ? new DependencyLoader(this) : loader);
  }

  @Override
  public void initialize() throws ExecutionException, InterruptedException {

    Set.of(
            this.libraryPath,
            this.dependencyPath,
            this.httpServerPath,
            this.vlcPath,
            this.imagePath,
            this.audioPath,
            this.videoPath)
        .forEach(
            ThrowingConsumer.unchecked(Files::createDirectories));

    this.registrationListener = new RegistrationListener(this);

    final Optional<PacketHandler> optional = NMSReflectionHandler.getNewPacketHandlerInstance();
    if (optional.isPresent()) {
      this.handler = optional.orElseThrow(AssertionError::new);
      new TinyProtocol(this.plugin) {
        @Override
        public Object onPacketOutAsync(
            final Player player, final Channel channel, final Object packet) {
          return EzMediaCore.this.handler.onPacketInterceptOut(player, packet);
        }

        @Override
        public Object onPacketInAsync(
            final Player player, final Channel channel, final Object packet) {
          return EzMediaCore.this.handler.onPacketInterceptIn(player, packet);
        }
      };
    } else {
      throw new LibraryException("Unsupported library version! Only supports 1.16.5 - 1.17!");
    }

    this.loader.start();

    DitherLookupUtil.init();

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
