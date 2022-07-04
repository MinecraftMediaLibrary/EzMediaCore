package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpDaemonSolution;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PackCallback {

  void stopAudio();

  void playAudio();

  @NotNull String getAudioUrl();

  @Nullable CompletableFuture<Path> getPackFuture();

  @Nullable HttpDaemonSolution getServer();
}
