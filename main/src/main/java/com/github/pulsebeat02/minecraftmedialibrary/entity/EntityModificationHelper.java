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

package com.github.pulsebeat02.minecraftmedialibrary.entity;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for plugins to be able to modify a specific entity using commands only. For
 * example,
 *
 * <p>/mob set [ENTITY_TYPE] - Sets the type of entity. (Create a new EntityModificationHelper)
 *
 * <p>/mob modify getName "custom name" - Sets the name of the entity. (call the
 * EntityModification#invokeGetter with the name of the method (getName) and the argument ("custom
 * name"))
 */
public class EntityModificationHelper<T extends Entity> implements EntityAttributeMutator<T> {

  private final Class<?> entityClass;
  private final Map<String, Method> methods;
  private final Set<Class<?>> validArguments;

  /**
   * Instantiates a new EntityModificationHelper.
   *
   * @param clazz the entity type
   */
  public EntityModificationHelper(@NotNull final Class<?> clazz) {
    entityClass = clazz;
    methods = getProperMethods();
    validArguments =
        ImmutableSet.<Class<?>>builder()
            .add(byte.class)
            .add(short.class)
            .add(int.class)
            .add(long.class)
            .add(float.class)
            .add(double.class)
            .add(boolean.class)
            .add(char.class)
            .add(String.class)
            .build();
  }

  /**
   * Gets the proper methods from the specific entity class.
   *
   * @return the map containing the method names and actual methods itself
   */
  private Map<String, Method> getProperMethods() {
    final Map<String, Method> methods = new HashMap<>();
    for (final Method method : entityClass.getDeclaredMethods()) {
      final Class<?>[] args = method.getParameterTypes();
      final String name = method.getName();
      if (name.startsWith("get") && args.length == 1 && validArguments.contains(args[0])) {
        methods.put(name, method);
      }
    }
    return methods;
  }

  @Override
  public boolean invokeGetter(
      @NotNull final T entity, @NotNull final String methodName, @NotNull final Object argument) {
    if (!methods.containsKey(methodName)) {
      return false;
    }
    try {
      methods.get(methodName).invoke(entity, argument);
      return true;
    } catch (final IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Class<?> getEntityClass() {
    return entityClass;
  }

  @Override
  public Map<String, Method> getMethodMap() {
    return methods;
  }

  @Override
  public Set<Class<?>> getValidArguments() {
    return validArguments;
  }

  @Override
  public Collection<Method> getRawMethods() {
    return methods.values();
  }

  @Override
  public Collection<String> getMethodNames() {
    return methods.keySet();
  }
}
