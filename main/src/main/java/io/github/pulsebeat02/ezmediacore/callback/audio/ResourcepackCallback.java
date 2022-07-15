package io.github.pulsebeat02.ezmediacore.callback.audio;

import static org.bukkit.SoundCategory.MASTER;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.extraction.AudioAttributes;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.OGGAudioExtractor;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpDaemonSolution;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.throwable.HttpServerException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResourcepackCallback extends SampleCallback implements PackCallback {

  private HttpServer server;
  private CompletableFuture<Path> extraction;

  private final SoundKey key;
  private final AudioConfiguration configuration;
  private final Path path;
  private final String host;
  private final int port;
  private final String ogg;

  ResourcepackCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final Viewers viewers,
      @Nullable final SoundKey key,
      @NotNull final Path path,
      @NotNull final String host,
      final int port) {
    super(core, viewers);
    this.configuration = configuration;
    this.key = this.getInternalSoundKey(key);
    this.path = path;
    this.host = host;
    this.port = port;
    this.ogg = core.getHttpServerPath().resolve("output.ogg").toString();
  }

  private @NotNull SoundKey getInternalSoundKey(@Nullable final SoundKey key) {
    if (key == null) {
      return SoundKey.ofSound(
          this.getCore().getPlugin().getName().toLowerCase(java.util.Locale.ROOT));
    }
    return key;
  }

  @Override
  public void process(final byte @NotNull [] data) {}

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {

    final MediaLibraryCore core = this.getCore();
    final String source = player.getInput().getDirectAudioMrl().toString();

    if (this.server == null) {
      this.server = HttpServer.ofServer(core, this.path, this.host, this.port, true);
      this.server.startServer();
    }

    if (status == PlayerControls.START) {
      final OGGAudioExtractor extractor =
          OGGAudioExtractor.ofFFmpegAudioExtractor(core, this.configuration, source, this.ogg);
      this.extraction = extractor.executeAsync().thenApply(s -> this.createPack());
    }

    if (status == PlayerControls.START || status == PlayerControls.RESUME) {
      this.playAudio();
    } else {
      this.stopAudio();
    }
  }

  private @NotNull Path createPack() {
    try {
      final ResourcepackSoundWrapper wrapper = this.createWrapper();
      return wrapper.getResourcepackFilePath();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private @NotNull ResourcepackSoundWrapper createWrapper() throws IOException {

    final String sound = this.key.getName();
    final Path target = this.path.resolve("audio.zip");
    final int id = PackFormat.getCurrentFormat().getId();

    final ResourcepackSoundWrapper wrapper =
        ResourcepackSoundWrapper.ofSoundPack(target, "Auto-Generated Audio Pack", id);
    wrapper.addSound(sound, Path.of(this.ogg));
    wrapper.wrap();

    return wrapper;
  }

  @Override
  public void stopAudio() {
    final Viewers viewers = this.getWatchers();
    for (final Player player : viewers.getPlayers()) {
      player.stopSound(this.key.getName(), MASTER);
    }
  }

  @Override
  public void playAudio() {
    final Viewers viewers = this.getWatchers();
    for (final Player player : viewers.getPlayers()) {
      player.playSound(player.getLocation(), this.key.getName(), MASTER, 1.0f, 1.0f);
    }
  }

  @Override
  public @NotNull String getAudioUrl() {
    if (this.server == null) {
      throw new HttpServerException("Server has not been started yet!");
    }
    return this.server.createUrl("index.html");
  }

  @Override
  public @Nullable HttpDaemonSolution getServer() {
    return this.server;
  }

  @Override
  public @Nullable CompletableFuture<Path> getPackFuture() {
    return this.extraction;
  }

  @Override
  public void close() throws Exception {
    if (this.server != null) {
      this.server.stopServer();
    }
    if (this.extraction != null) {
      this.extraction.cancel(true);
    }
  }

  public static final class Builder extends AudioCallbackBuilder {

    private SoundKey key;
    private AudioConfiguration configuration;
    private Path path;
    private String host;
    private int port;

    {
      this.key = SoundKey.ofSound("emc");
      this.configuration = AudioAttributes.OGG_CONFIGURATION;
    }

    public Builder() {}

    @Contract("_ -> this")
    public @NotNull Builder key(@NotNull final SoundKey key) {
      this.key = key;
      return this;
    }

    @Contract("_ -> this")
    public Builder host(@NotNull final String host) {
      this.host = host;
      return this;
    }

    @Contract("_ -> this")
    public Builder port(final int port) {
      this.port = port;
      return this;
    }

    @Contract("_ -> this")
    public Builder path(@NotNull final Path path) {
      this.path = path;
      return this;
    }

    @Contract("_ -> this")
    public Builder configuration(@NotNull final AudioConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    @Override
    public @NotNull AudioCallback build(@NotNull final MediaLibraryCore core) {
      final Viewers viewers = this.getViewers();
      return new ResourcepackCallback(
          core, this.configuration, viewers, this.key, this.path, this.host, this.port);
    }
  }
}
