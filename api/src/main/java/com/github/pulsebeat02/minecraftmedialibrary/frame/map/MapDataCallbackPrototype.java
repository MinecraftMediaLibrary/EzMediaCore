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

package com.github.pulsebeat02.minecraftmedialibrary.frame.map;

import com.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallbackAttribute;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitheringAlgorithm;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;

import java.util.UUID;

/** A prototype of the MapDataCallback (pre-mature). */
public interface MapDataCallbackPrototype extends FrameCallback, FrameCallbackAttribute {

  /**
   * Get viewers uuid [ ].
   *
   * @return the uuid [ ]
   */
  UUID[] getViewers();

  /**
   * Gets map.
   *
   * @return the map
   */
  long getMap();

  /**
   * Gets the PacketHandler.
   *
   * @return the library
   */
  PacketHandler getHandler();

  /**
   * Gets the dithering type.
   *
   * @return the dithering type
   */
  DitheringAlgorithm getType();
}
