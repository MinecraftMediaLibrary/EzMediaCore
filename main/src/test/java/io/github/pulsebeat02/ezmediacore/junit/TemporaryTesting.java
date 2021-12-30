package io.github.pulsebeat02.ezmediacore.junit;

import io.github.pulsebeat02.ezmediacore.player.input.implementation.DesktopInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.DeviceInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;

public final class TemporaryTesting {

  public static void main(final String[] args) {
    UrlInput.emptyUrl();
    PathInput.emptyPath();
    WindowInput.emptyWindow();
    DesktopInput.defaultDesktop();
    MrlInput.emptyMrl();
    DeviceInput.emptyDevice();
  }
}
