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

package com.github.pulsebeat02.minecraftmedialibrary.image.basic;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.image.DrawableMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public interface StaticImageProxy extends DrawableMap, ConfigurationSerializable {

  /** Draws the specific image on the map id. */
  @Override
  void drawImage();

  /** Called when an image is being draw on a map. */
  @Override
  void onDrawImage();

  /**
   * Serializes a MapImage.
   *
   * @return map of serialized values
   */
  @Override
  @NotNull
  Map<String, Object> serialize();

  /**
   * Gets library.
   *
   * @return the library
   */
  MediaLibrary getLibrary();

  /**
   * Gets map.
   *
   * @return the map
   */
  int getMap();

  /**
   * Gets image.
   *
   * @return the image
   */
  File getImage();

  /**
   * Gets height.
   *
   * @return the height
   */
  int getHeight();

  /**
   * Gets width.
   *
   * @return the width
   */
  int getWidth();
}
