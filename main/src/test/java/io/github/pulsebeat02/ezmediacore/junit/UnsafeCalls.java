package io.github.pulsebeat02.ezmediacore.junit;

import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.pulsebeat02.ezmediacore.junit.util.DontInlineMeBaby;
import io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils;
import org.junit.jupiter.api.Test;

public final class UnsafeCalls {

  private static final DontInlineMeBaby DONT_INLINE;

  static {
    DONT_INLINE = new DontInlineMeBaby();
  }

  @Test
  public void unsafeCalls() {
    this.normalFinalField();
    this.normalStaticFinalField();
  }

  private void normalFinalField() {
    try {
      UnsafeUtils.setFinalField(DontInlineMeBaby.class.getDeclaredField("obj"), DONT_INLINE, null);
      assertNull(DONT_INLINE.getObj());
    } catch (final NoSuchFieldException e) {
      throw new AssertionError("Could not find obj value in DontInlineMeBaby!");
    }
  }

  private void normalStaticFinalField() {
    try {
      UnsafeUtils.setStaticFinalField(UnsafeCalls.class.getDeclaredField("DONT_INLINE"), null);
      assertNull(DONT_INLINE);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError("Could not find DONT_INLINE value in UnsafeCalls!");
    }
  }
}
