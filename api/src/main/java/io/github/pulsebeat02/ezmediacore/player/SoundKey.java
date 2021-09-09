package io.github.pulsebeat02.ezmediacore.player;

import org.bukkit.Sound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SoundKey {

  private static final SoundKey EMPTY_SOUND_KEY;

  static {
    EMPTY_SOUND_KEY = ofSound("");
  }

  private final String name;

  SoundKey(@NotNull final String name) {
    this.name = name;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull SoundKey ofSound(@NotNull final String sound) {
    return new SoundKey(sound);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull SoundKey ofSound(@NotNull final Sound sound) {
    return ofSound(sound.getKey().getNamespace());
  }

  public static @NotNull SoundKey emptySoundKey() {
    return EMPTY_SOUND_KEY;
  }

  public @NotNull String getName() {
    return this.name;
  }
}
