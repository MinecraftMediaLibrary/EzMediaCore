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

package io.github.pulsebeat02.minecraftmedialibrary.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface EntityAttributeMutator<T extends Entity> {

  /**
   * Invokes a specific argument based off the method name.
   *
   * @param entity the entity
   * @param methodName the method name
   * @param argument the argument
   * @return true if the operation was successful, false otherwise
   */
  boolean invokeGetter(
      @NotNull final T entity, @NotNull final String methodName, @NotNull final Object argument);

  /**
   * Gets the entity class.
   *
   * @return the entity class
   */
  Class<?> getEntityClass();

  /**
   * Gets the String/method map.
   *
   * @return the String/method map
   */
  Map<String, Method> getMethodMap();

  /**
   * Gets the possible valid arguments for methods.
   *
   * @return the valid arguments
   */
  Set<Class<?>> getValidArguments();

  /**
   * Gets the raw methods from the map.
   *
   * @return the raw methods
   */
  Collection<Method> getRawMethods();

  /**
   * Gets the method names from the map.
   *
   * @return the method names
   */
  Collection<String> getMethodNames();
}
