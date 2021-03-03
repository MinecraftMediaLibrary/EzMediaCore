/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;

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
