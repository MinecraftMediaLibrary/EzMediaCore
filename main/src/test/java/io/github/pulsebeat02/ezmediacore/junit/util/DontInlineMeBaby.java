package io.github.pulsebeat02.ezmediacore.junit.util;

import org.jetbrains.annotations.Nullable;

public final class DontInlineMeBaby {

  private final Object obj;

  public DontInlineMeBaby() {
    this.obj = new Object();
  }

  public Object getObj() {
    return this.obj;
  }
}
