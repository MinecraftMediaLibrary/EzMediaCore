package io.github.pulsebeat02.ezmediacore.ffmpeg;

import org.jetbrains.annotations.NotNull;

public interface ExposedProcess {

  @NotNull
  Process getProcess();
}
