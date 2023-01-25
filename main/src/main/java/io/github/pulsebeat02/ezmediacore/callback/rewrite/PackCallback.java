package io.github.pulsebeat02.ezmediacore.callback.rewrite;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.audio.PackSource;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.OGGAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.SoundCategory.MASTER;

public final class PackCallback extends AudioHandler implements PackSource {

  private final HttpServer server;
  private final AudioConfiguration configuration;
  private final Viewers viewers;
  private final SoundKey key;
  private final Path ogg;
  private CompletableFuture<Object> future;

  PackCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final Viewers viewers,
      @Nullable final SoundKey key,
      @NotNull final Path path,
      @NotNull final String host,
      final int port) {
    super(core);
    this.configuration = configuration;
    this.viewers = viewers;
    this.key = this.getInternalSoundKey(key);
    this.server = HttpServer.ofServer(core, path, host, port, true);
    this.ogg = core.getHttpServerPath().resolve("output.ogg");
  }

  private @NotNull SoundKey getInternalSoundKey(@Nullable final SoundKey key) {
    if (key == null) {
      final String name = this.getCore().getPlugin().getName().toLowerCase(java.util.Locale.ROOT);
      return SoundKey.ofSound(name);
    }
    return key;
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    this.startServer();
    this.handleStart(player, status);
    this.handleAudio(status);
  }

  private void handleAudio(@NotNull final PlayerControls status) {
    if (status == PlayerControls.START || status == PlayerControls.RESUME) {
      this.playAudio();
    } else {
      this.stopAudio();
    }
  }

  private void stopAudio() {
    final Viewers viewers = this.viewers;
    for (final Player player : viewers.getPlayers()) {
      player.stopSound(this.key.getName(), MASTER);
    }
  }

  private void playAudio() {
    final Viewers viewers = this.viewers;
    for (final Player player : viewers.getPlayers()) {
      player.playSound(player.getLocation(), this.key.getName(), MASTER, 1.0f, 1.0f);
    }
  }

  private void handleStart(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls controls) {
    if (controls == PlayerControls.START) {
      final String source = player.getInput().getDirectAudioMrl().toString();
      final OGGAudioExtractor extractor = this.createExtractor(source);
      this.future = extractor.executeAsync().thenApply(pack -> this.executeWrapper());
    }
  }

  private @NotNull ResourcepackSoundWrapper executeWrapper() {
    final String sound = this.key.getName();
    final Path http = this.server.getDaemon().getServerPath();
    final Path target = http.resolve("audio.zip");
    final int id = PackFormat.getCurrentFormat().getId();
    return this.wrapResourcepack(sound, target, id);
  }

  @NotNull
  private ResourcepackSoundWrapper wrapResourcepack(
      @NotNull final String sound, @NotNull final Path target, final int id) {
    final ResourcepackSoundWrapper wrapper = createWrapper(target, id);
    wrapper.addSound(sound, this.ogg);
    try {
      wrapper.wrap();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
    return wrapper;
  }

  @NotNull
  private static ResourcepackSoundWrapper createWrapper(@NotNull final Path target, final int id) {
    return ResourcepackSoundWrapper.ofSoundPack(target, "Auto-Generated Audio Pack", id);
  }

  @NotNull
  private OGGAudioExtractor createExtractor(@NotNull final String source) {
    final MediaLibraryCore core = this.getCore();
    return OGGAudioExtractor.ofFFmpegAudioExtractor(
        core, this.configuration, Path.of(source), this.ogg);
  }

  private void startServer() {
    if (!this.server.isRunning()) {
      this.server.startServer();
    }
  }

  @Override
  public void process(final byte @NotNull [] data) {}

  @Override
  public @NotNull CompletableFuture<Object> getFuture() {
    return this.future;
  }
}
