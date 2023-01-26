package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public abstract class MediaPlayerInputParser implements InputParser {

  protected static final String[] EMPTY_ARGS;

  static {
    EMPTY_ARGS = new String[]{};
  }

  private final MediaLibraryCore core;
  private final boolean windows;

  public MediaPlayerInputParser(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.windows = core.getDiagnostics().getSystem().getOSType() == OSType.WINDOWS;
  }

  @Override
  public @NotNull Pair<Object, String[]> parseInput(@NotNull final Input input) {
    if (input instanceof UrlInput urlInput) {
      return this.parseUrl(urlInput);
    } else if (input instanceof PathInput pathInput) {
      return this.parsePath(pathInput);
    } else if (input instanceof DeviceInput deviceInput) {
      return this.parseDevice(deviceInput);
    } else if (input instanceof MrlInput mrlInput) {
      return this.parseMrl(mrlInput);
    } else if (input instanceof DesktopInput desktopInput) {
      return this.parseDesktop(desktopInput);
    } else if (input instanceof WindowInput windowInput) {
      return this.parseWindow(windowInput);
    } else {
      return Pair.ofPair(input.getInput(), new String[]{});
    }
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  public boolean isWindows() {
    return this.windows;
  }
}
