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

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;

/**
 * Special collection utilities used throughout the library and also open to users. Used for easier
 * collection management.
 */
public class CollectionUtilities {

  /**
   * Creates a Guava multimap using the input map.
   *
   * @param <K> the type parameter
   * @param <V> the type parameter
   * @param input the input
   * @return the multimap
   */
  public static <K, V> Multimap<K, V> createMultiMap(final Map<K, ? extends Iterable<V>> input) {
    final Multimap<K, V> multimap = ArrayListMultimap.create();
    for (final Map.Entry<K, ? extends Iterable<V>> entry : input.entrySet()) {
      multimap.putAll(entry.getKey(), entry.getValue());
    }
    return multimap;
  }
}
