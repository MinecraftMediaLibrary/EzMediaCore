package io.github.pulsebeat02.epicmedialib.resourcepack;

import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface SoundPackWrapper {

  void addSound(@NotNull final String key, @NotNull final Path path);

  void removeSound(@NotNull final String key);

  byte[] createSoundJson();

  @NotNull
  Map<String, Path> listSounds();
}
