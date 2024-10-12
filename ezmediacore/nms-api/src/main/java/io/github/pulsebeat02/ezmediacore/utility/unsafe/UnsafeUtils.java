/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.utility.unsafe;

import java.lang.reflect.Field;


import sun.misc.Unsafe;

public final class UnsafeUtils {

  private static final Unsafe UNSAFE;

  static {
    UNSAFE = UnsafeProvider.getUnsafe();
  }

  private UnsafeUtils() {}

  /**
   * Sets a specific final field for a class (not static!).
   *
   * @param field the final field
   * @param obj the object to set the final field on
   * @param value the value
   */
  public static void setFinalField(
       final Field field,  final Object obj,  final Object value) {
    UNSAFE.putObject(obj, UNSAFE.objectFieldOffset(field), value);
  }

  /**
   * Sets a specific static final field for a class.
   *
   * @param field the static final field
   * @param value the value
   */
  public static void setStaticFinalField( final Field field,  final Object value) {
    UNSAFE.putObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field), value);
  }

  public static  Object getFieldExceptionally(
       final Object object,  final String name) {
    try {
      return getField(object, name);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  public static  Object getFieldExceptionally(
       final Class<?> clazz,  final Object object,  final String name) {
    try {
      return getField(clazz, object, name);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  public static  Object getField( final Object object,  final String name)
      throws NoSuchFieldException {
    return getField(object.getClass(), object, name);
  }

  public static  Object getField(
       final Class<?> clazz,  final Object object,  final String name)
      throws NoSuchFieldException {
    return UNSAFE.getObject(object, UNSAFE.objectFieldOffset(clazz.getDeclaredField(name)));
  }
}
