package io.github.pulsebeat02.ezmediacore.analysis;

import org.jetbrains.annotations.NotNull;

/**
 * This class contains information relative to the CPU. This includes the CPU architecture and
 * whether the CPU uses 64 bits or 32 bits or not.
 */
public interface CpuInfo {

  /**
   * Returns the architecture (for example AMD64, I386, etc) of the CPU.
   *
   * @return the architecture of the CPU
   */
  @NotNull
  String architecture();

  /**
   * Returns whether the CPU uses 64 bits.
   *
   * @return true if the CPU uses 64 bits, false otherwise
   */
  boolean bits64();
}
