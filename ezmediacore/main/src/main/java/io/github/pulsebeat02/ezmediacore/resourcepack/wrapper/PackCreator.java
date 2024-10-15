package io.github.pulsebeat02.ezmediacore.resourcepack.wrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public final class PackCreator {

  private final Path target;
  private final String description;
  private final Collection<SoundResource> sounds;
  private final PackWrapper wrapper;

  public PackCreator(final Path target,
                     final String description,
                     final Collection<SoundResource> sounds) {
    this.wrapper = new PackWrapper();
    this.target = target;
    this.description = description;
    this.sounds = sounds;
  }

  public Path writePack() {
    try {
      this.writeMetaFile();
      this.writeSoundFiles();
      return this.wrapper.wrap(this.target);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void writeSoundFiles() {
    for (final SoundResource resource : this.sounds) {
      final byte[] bytes = resource.getBytes();
      final String key = resource.getKey();
      final String name = resource.getName();
      this.wrapper.writeSound(bytes, key, name);
    }
  }

  private void writeMetaFile() {
    final PackFormat current = PackFormat.getCurrentFormat();
    final int format = current.getId();
    this.wrapper.writeDescription(this.description);
    this.wrapper.writeFormat(format);
  }

  public static PackCreator create(final Path target, final SoundResource sound) {
    return new PackCreator(target, "EzMediaCore Resource Pack", Set.of(sound));
  }

  public static PackCreator create(final Path target,
                                   final Collection<SoundResource> sounds) {
    return new PackCreator(target, "EzMediaCore Resource Pack", sounds);
  }

  public static PackCreator create(final Path target,
                                   final String description,
                                   final Collection<SoundResource> sounds) {
    return new PackCreator(target, description, sounds);
  }
}
