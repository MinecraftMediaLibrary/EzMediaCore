package io.github.pulsebeat02.ezmediacore.resourcepack.wrapper;

import io.github.pulsebeat02.ezmediacore.pipeline.input.downloader.DownloadableInput;
import io.github.pulsebeat02.ezmediacore.transcode.FFmpegAudioExtractor;
import io.github.pulsebeat02.ezmediacore.util.io.FileUtils;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.github.pulsebeat02.ezmediacore.util.io.FileUtils.createTempFile;

public final class SoundMedia implements SoundResource {

  private final byte[] bytes;
  private final String key;
  private final String name;

  public SoundMedia(final byte[] bytes, final String key, final String name) {
    this.bytes = bytes;
    this.key = key;
    this.name = name;
  }

  public static CompletableFuture<SoundMedia> encodeToVorbisOGG(final DownloadableInput input) {
    return encodeToVorbisOGG(input, "audio", "sound");
  }

  public static CompletableFuture<SoundMedia> encodeToVorbisOGG(final DownloadableInput input, final String key, final String name) {
    final FFmpegAudioExtractor extractor = new FFmpegAudioExtractor(input);
    final UUID random = UUID.randomUUID();
    final String prefix = random.toString();
    final String suffix = ".ogg";
    final Path temp = createTempFile(prefix, suffix);
    return extractor.extractAudio(temp, "libvorbis", "ogg").thenApply(path -> createMedia(path, key, name));
  }

  private static SoundMedia createMedia(final Path path, final String key, final String name) {
    final byte[] bytes = FileUtils.readAllBytes(path);
    return new SoundMedia(bytes, key, name);
  }

  @Override
  public byte[] getBytes() {
    return this.bytes;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
