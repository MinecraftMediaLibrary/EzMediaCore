package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import org.jetbrains.annotations.NotNull;

public interface ResourcepackResult {

  @NotNull
  String getUrl();

  @NotNull
  String getHash();
}
