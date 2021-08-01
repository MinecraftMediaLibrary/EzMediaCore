package io.github.pulsebeat02.epicmedialib.ffmpeg;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface IOProvider {

  @NotNull
  Path getInput();

  @NotNull
  Path getOutput();
}
