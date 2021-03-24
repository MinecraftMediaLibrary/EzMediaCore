/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * An enum containing all the supported CPU Architectures for each of the Linux packages. Useful
 * for easier management.
 */
public enum CPUArchitecture {

  /** AMD64 CPU Architecture. */
  AMD64,

  /** AARCH64 CPU Architecture. */
  AARCH64,

  /** ARMHF CPU Architecture. */
  ARMHF,

  /** ARMV7H CPU Architecture. */
  ARMV7H,

  /** ARMHFP CPU Architecture. */
  ARMHFP,

  /** ARMV7HL CPU Architecture. */
  ARMV7HL,

  /** ARMV7 CPU Architecture. */
  ARMV7,

  /** ARM64 CPU Architecture. */
  ARM64,

  /** EARNMV7HF CPU Architecture. */
  EARNMV7HF,

  /** X86_64 CPU Architecture. */
  X86_64,

  /** I386 CPU Architecture. */
  I386,

  /** I586 CPU Architecture. */
  I586,

  /** I486 CPU Architecture. */
  I486;

  static {
    Logger.info("Listing All Possible CPU Architectures...");
    Arrays.stream(values()).forEach(x -> Logger.info(x.name()));
  }

  /**
   * Gets CPUArchitecture from name.
   *
   * @param name the name
   * @return the cpu architecture
   */
  public static CPUArchitecture fromName(@NotNull final String name) {
    for (final CPUArchitecture val : values()) {
      if (val.name().equals(name)) {
        return val;
      }
    }
    return null;
  }
}
