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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

/** The specific pack format version for each resourcepacks. Varies per Minecraft version. */
public enum PackFormatVersioning {

  /** 1.15 pack format versioning. */
  VER_1_15(5),

  /** 1.15.1 Pack Format */
  VER_1_15_1(5),

  /** 1.15.2 Pack Format */
  VER_1_15_2(5),

  /** 1.16.1 Pack Format */
  VER_1_16_1(5),

  /** 1.16.2 Pack Format */
  VER_1_16_2(6),

  /** 1.16.3 Pack Format */
  VER_1_16_3(6),

  /** 1.16.4 Pack Format */
  VER_1_16_4(6),

  /** 1.16.5 Pack Format */
  VER_1_16_5(6);

  private final int packFormat;

  /**
   * Instantiates a pack format.
   *
   * @param id format id
   */
  PackFormatVersioning(final int id) {
    packFormat = id;
  }

  /**
   * Gets pack format id.
   *
   * @return the pack format id
   */
  public int getPackFormatID() {
    return packFormat;
  }
}
