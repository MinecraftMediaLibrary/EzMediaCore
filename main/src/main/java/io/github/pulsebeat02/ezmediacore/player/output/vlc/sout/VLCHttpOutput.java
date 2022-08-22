package io.github.pulsebeat02.ezmediacore.player.output.vlc.sout;

import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCStandardOutput;
import org.jetbrains.annotations.NotNull;

public class VLCHttpOutput extends VLCStandardOutput {

  public VLCHttpOutput(@NotNull final String section) {
    super(section);
  }
}
