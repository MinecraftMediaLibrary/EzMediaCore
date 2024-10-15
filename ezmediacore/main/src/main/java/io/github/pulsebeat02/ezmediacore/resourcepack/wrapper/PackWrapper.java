package io.github.pulsebeat02.ezmediacore.resourcepack.wrapper;

import io.github.pulsebeat02.ezmediacore.util.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class PackWrapper {

  private final Map<String, byte[]> files;
  private final PackMeta meta;
  private final SoundMeta sounds;

  public PackWrapper() {
    this.files = new HashMap<>();
    this.meta = new PackMeta();
    this.sounds = new SoundMeta();
  }

  public void writeDescription(final String description) {
    this.meta.writeDescription(description);
  }

  public void writeFormat(final int format) {
    this.meta.writeFormat(format);
  }

  public void writeSound(final Path sound, final String soundKey, final String soundName) {
    try {
      final byte[] bytes = Files.readAllBytes(sound);
      this.writeSound(bytes, soundKey, soundName);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public void writeSound(final byte[] sound, final String soundKey, final String soundName) {
    this.sounds.writeSound(soundKey, soundName);
    final String file = "assets/minecraft/sounds/" + soundKey + ".ogg";
    this.files.put(file, sound);
  }

  public void writeFile(final byte[] contents, final String path) {
    this.files.put(path, contents);
  }

  public Path wrap(final Path target) throws IOException {
    try (final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(target))) {
      FileUtils.createFileIfNotExists(target);
      final byte[] mcmeta = this.meta.serialize();
      final byte[] soundMeta = this.sounds.serialize();
      this.files.put("pack.mcmeta", mcmeta);
      this.files.put("assets/minecraft/sounds.json", soundMeta);
      final Set<Map.Entry<String, byte[]>> entries = this.files.entrySet();
      for (final Map.Entry<String, byte[]> entry : entries) {
        final String name = entry.getKey();
        final byte[] data = entry.getValue();
        final ZipEntry zipEntry = new ZipEntry(name);
        out.putNextEntry(zipEntry);
        out.write(data);
        out.closeEntry();
      }
    }
    return target;
  }
}
