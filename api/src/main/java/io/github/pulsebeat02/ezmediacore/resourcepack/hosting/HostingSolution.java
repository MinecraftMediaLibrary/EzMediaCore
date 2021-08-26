package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;

@FunctionalInterface
public interface HostingSolution {

  @NotNull
  String createUrl(@NotNull final String input);

  @NotNull
  default String getName() {
    return "EzMediaCore Custom Solution";
  }
}
