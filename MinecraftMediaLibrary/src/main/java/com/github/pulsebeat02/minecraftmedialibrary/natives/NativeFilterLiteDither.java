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

package com.github.pulsebeat02.minecraftmedialibrary.natives;

import java.nio.ByteBuffer;

public class NativeFilterLiteDither {

  static {
    System.loadLibrary("filterlite-dither");
  }

  /**
   * The native method used for setup which calls a C++ method in the filterlite-dither.so/.dll
   * file.
   *
   * @param colorMap the passed in color map
   * @param fullColorMap the passed in full color map
   */
  private native void setup(final int[] colorMap, final int[] fullColorMap);

  /**
   * The native method used to dither a specific array of data that calls a C++ method in the
   * filterlite-dither.so/.dll file.
   *
   * @param buffer the buffer to edit
   * @param data the image data to read
   * @param width the width passed in
   */
  private native void dither_native(final ByteBuffer buffer, final int[] data, final int width);
}
