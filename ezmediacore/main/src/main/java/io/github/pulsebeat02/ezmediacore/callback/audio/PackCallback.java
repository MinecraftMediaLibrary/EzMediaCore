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
package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.OGGAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.SoundCategory.MASTER;

public final class PackCallback extends AudioOutput implements PackSource {

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
      @NotNull final String host,
      final int port) {
    super(core);
    this.configuration = configuration;
    this.viewers = viewers;
    this.key = this.getInternalSoundKey(key);
    this.server = this.getServer(core, host, port);
    this.ogg = core.getHttpServerPath().resolve("output.ogg");
  }

  private HttpServer getServer(
      @NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
    try {
      return HttpServer.ofServer(core, host, port, true);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
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

  public static final class Builder extends ServerCallback.Builder {

    private AudioConfiguration configuration;
    private Viewers viewers = Viewers.onlinePlayers();
    private SoundKey key;

    @Contract("_ -> this")
    public @NotNull Builder host(@NotNull final AudioConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder viewers(@NotNull final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder key(@NotNull final SoundKey key) {
      this.key = key;
      return this;
    }

    @Contract("_ -> new")
    @Override
    public @NotNull AudioOutput build(@NotNull final MediaLibraryCore core) {
      final String host = this.getHost();
      final int port = this.getPort();
      return new PackCallback(core, this.configuration, this.viewers, this.key, host, port);
    }
  }
}
