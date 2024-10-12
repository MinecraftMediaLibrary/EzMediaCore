package io.github.pulsebeat02.ezmediacore.capabilities;

import java.util.function.Supplier;

public class Capability {

  private final boolean enabled;

  public Capability(final Supplier<Boolean> verify) {
    this.enabled = verify.get();
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public boolean isDisabled() {
    return !this.enabled;
  }
}
