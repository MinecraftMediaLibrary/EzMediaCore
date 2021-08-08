package io.github.pulsebeat02.ezmediacore.vlc.os.unix.dependency;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

class UnixDependency {

  private final String name;
  private final String url;
  private final Set<UnixDependency> dependencies;

  UnixDependency(@NotNull final String name, @NotNull final String url,
      @NotNull final Set<UnixDependency> dependencies) {
    this.name = name;
    this.url = url;
    this.dependencies = dependencies;
  }

  public String getName() {
    return this.name;
  }

  public String getUrl() {
    return this.url;
  }

  public Set<UnixDependency> getDependencies() {
    return this.dependencies;
  }
}
