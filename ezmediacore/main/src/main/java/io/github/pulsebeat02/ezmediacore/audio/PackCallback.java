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
package io.github.pulsebeat02.ezmediacore.audio;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import rewrite.pipeline.output.Viewers;
import io.github.pulsebeat02.ezmediacore.ffmpeg.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.OGGAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.provider.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.io.HashingUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;



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
  private CompletableFuture<Void> future;

  PackCallback(
       final EzMediaCore core,
       final AudioConfiguration configuration,
       final Viewers viewers,
       final SoundKey key,
       final String host,
      final int port) {
    super(core);
    this.configuration = configuration;
    this.viewers = viewers;
    this.key = this.getInternalSoundKey(key);
    this.server = this.getServer(core, host, port);
    this.ogg = core.getHttpServerPath().resolve("output.ogg");
  }

  private HttpServer getServer(
       final EzMediaCore core,  final String host, final int port) {
    try {
      return HttpServer.ofServer(core, host, port, true);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private  SoundKey getInternalSoundKey( final SoundKey key) {
    if (key == null) {
      final String name = this.getCore().getPlugin().getName().toLowerCase(java.util.Locale.ROOT);
      return SoundKey.ofSound(name);
    }
    return key;
  }

  @Override
  public void preparePlayerStateChange(
       final VideoPlayer player,  final PlayerControls status) {
    this.startServer();
    this.handleStart(player, status);
    this.handleAudio(status);
  }

  private void handleAudio( final PlayerControls status) {
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
       final VideoPlayer player,  final PlayerControls controls) {
    if (controls == PlayerControls.START) {
      final String source = player.getInput().getDirectAudioMrl().toString();
      final OGGAudioExtractor extractor = this.createExtractor(source);
      this.future =
          extractor
              .executeAsync()
              .thenApply(pack -> this.executeWrapper())
              .thenAccept(this::sendPack);
    }
  }

  private void sendPack( final ResourcepackSoundWrapper wrapper) {
    final Path target = wrapper.getResourcepackFilePath();
    final byte[] hash = HashingUtils.createHashSha1(target);
    final String url = this.server.createUrl(target);
    for (final Player p : this.viewers.getPlayers()) {
      p.setResourcePack(url, hash);
    }
  }

  private  ResourcepackSoundWrapper executeWrapper() {
    final String sound = this.key.getName();
    final Path http = this.server.getDaemon().getServerPath();
    final Path target = http.resolve("audio.zip");
    final int id = PackFormat.getCurrentFormat().getId();
    return this.wrapResourcepack(sound, target, id);
  }


  private ResourcepackSoundWrapper wrapResourcepack(
       final String sound,  final Path target, final int id) {
    final ResourcepackSoundWrapper wrapper = createWrapper(target, id);
    wrapper.addSound(sound, this.ogg);
    try {
      wrapper.wrap();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
    return wrapper;
  }


  private static ResourcepackSoundWrapper createWrapper( final Path target, final int id) {
    return ResourcepackSoundWrapper.ofSoundPack(target, "Auto-Generated Audio Pack", id);
  }


  private OGGAudioExtractor createExtractor( final String source) {
    final EzMediaCore core = this.getCore();
    return OGGAudioExtractor.ofFFmpegAudioExtractor(
        core, this.configuration, Path.of(source), this.ogg);
  }

  private void startServer() {
    if (!this.server.isRunning()) {
      this.server.startServer();
    }
  }

  @Override
  public void process(final byte  [] data) {}

  @Override
  public  CompletableFuture<Void> getFuture() {
    return this.future;
  }

  public static final class Builder extends ServerCallback.Builder {

    private AudioConfiguration configuration;
    private Viewers viewers = Viewers.onlinePlayers();
    private SoundKey key;

    @Contract("_ -> this")
    @Override
    public  Builder host( final String host) {
      super.host(host);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public  Builder port(final int port) {
      super.port(port);
      return this;
    }

    @Contract("_ -> this")
    public  Builder audio( final AudioConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    @Contract("_ -> this")
    public  Builder viewers( final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    @Contract("_ -> this")
    public  Builder key( final SoundKey key) {
      this.key = key;
      return this;
    }

    @Contract("_ -> new")
    @Override
    public  AudioOutput build( final EzMediaCore core) {
      final String host = this.getHost();
      final int port = this.getPort();
      return new PackCallback(core, this.configuration, this.viewers, this.key, host, port);
    }
  }
}
