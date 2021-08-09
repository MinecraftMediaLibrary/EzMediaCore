package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.analysis.Diagnostic;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyClient;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MediaLibraryCore {

  void initialize() throws ExecutionException, InterruptedException;

  void shutdown();

  @NotNull
  Plugin getPlugin();

  @NotNull
  PacketHandler getHandler();

  @NotNull
  Path getLibraryPath();

  @NotNull
  Path getHttpServerPath();

  @NotNull
  Path getDependencyPath();

  @NotNull
  Path getVlcPath();

  @NotNull
  Path getImagePath();

  @NotNull
  Path getAudioPath();

  @NotNull
  Path getVideoPath();

  @NotNull
  Path getFFmpegPath();

  void setFFmpegPath(@NotNull final Path path);

  @NotNull
  Diagnostic getDiagnostics();

  boolean isDisabled();

  @NotNull
  Listener getRegistrationHandler();

  void setRegistrationHandler(@NotNull final Listener listener);

  @NotNull
  LibraryLoader getLibraryLoader();

  @Nullable
  SpotifyClient getSpotifyClient();
}
