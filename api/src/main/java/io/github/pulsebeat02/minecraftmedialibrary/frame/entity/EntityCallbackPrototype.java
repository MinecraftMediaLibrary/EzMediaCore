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

package io.github.pulsebeat02.minecraftmedialibrary.frame.entity;

import io.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallbackAttribute;
import io.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** A prototype of the EntityCallback (pre-mature). */
public interface EntityCallbackPrototype extends FrameCallback, FrameCallbackAttribute {

  /**
   * Modifies the entity accordingly. Users may override the method if they would like to modify the
   * entity with custom attributes.
   *
   * @param entity the entity to modify
   * @return the modified entity
   */
  Entity modifyEntity(@NotNull Entity entity);

  /**
   * Get viewers uuid [ ].
   *
   * @return the uuid [ ]
   */
  UUID[] getViewers();

  /**
   * Gets the PacketHandler.
   *
   * @return the library
   */
  PacketHandler getHandler();

  /**
   * Gets location.
   *
   * @return the location
   */
  Location getLocation();

  /**
   * Gets the cloud entities.
   *
   * @return the entities
   */
  Entity[] getEntities();

  /**
   * Gets the char type used in the name.
   *
   * @return the char used
   */
  String getCharType();

  /**
   * Gets the type of entity used.
   *
   * @return the type of entity
   */
  ScreenEntityType getType();
}
