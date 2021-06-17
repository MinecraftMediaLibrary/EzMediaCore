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

package io.github.pulsebeat02.deluxemediaplugin.command.image;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImageProxy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ImageCommandAttributes {

  private final Set<StaticImageProxy> images;
  private final Set<UUID> listen;
  private final Set<String> extensions;
  private int width;
  private int height;

  public ImageCommandAttributes() {
    images = new HashSet<>();
    listen = new HashSet<>();
    extensions = ImmutableSet.of(".png", ".jpg", ".jpeg", ".tif", ".gif");
    width = 1;
    height = 1;
  }

  public Set<StaticImageProxy> getImages() {
    return images;
  }

  public Set<UUID> getListen() {
    return listen;
  }

  public Set<String> getExtensions() {
    return extensions;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(final int height) {
    this.height = height;
  }
}
