/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import org.jetbrains.annotations.NotNull;

/** An enum to store the possible dithering modes the user can take. */
public enum DitherSetting {

  /** Standard Minecraft Dithering */
  STANDARD_MINECRAFT_DITHER(new StandardDithering()),

  /** Sierra Filter Lite Dithering */
  SIERRA_FILTER_LITE_DITHER(new FilterLiteDither()),

  /** Bayer Ordered 2 Dimensional Dithering */
  BAYER_ORDERED_2_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeTwo)),

  /** Bayer Ordered 4 Dimensional Dithering */
  BAYER_ORDERED_4_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeFour)),

  /** Bayer Ordered 8 Dimensional Dithering */
  BAYER_ORDERED_8_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeEight)),

  /** Floyd Steinberg Dithering */
  FLOYD_STEINBERG_DITHER(new FloydImageDither());

  private final DitherHolder holder;

  DitherSetting(@NotNull final DitherHolder holder) {
    this.holder = holder;
  }

  /**
   * Returns DitherSetting from String.
   *
   * @param str input
   * @return setting
   */
  public static DitherSetting fromString(@NotNull final String str) {
    final String search = str.toUpperCase();
    for (final DitherSetting setting : DitherSetting.values()) {
      if (setting.name().equals(search)) {
        return setting;
      }
    }
    return null;
  }

  /**
   * Gets holder.
   *
   * @return the holder
   */
  public DitherHolder getHolder() {
    return holder;
  }
}
