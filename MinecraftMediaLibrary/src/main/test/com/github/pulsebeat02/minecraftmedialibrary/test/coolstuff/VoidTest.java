/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/23/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.coolstuff;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Some weird/bad/curiosity tests I wanted to try out.
 */
public class VoidTest {

  private static Void ELEMENT;

  static {
    try {
      final Constructor<Void> CONSTRUCTOR = Void.class.getDeclaredConstructor();
      CONSTRUCTOR.setAccessible(true);
      ELEMENT = CONSTRUCTOR.newInstance();
      CONSTRUCTOR.setAccessible(false);
    } catch (final NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  public static void main(final String[] args) {
    final List<Void> voidList = new ArrayList<>();
    voidList.add(ELEMENT);
    System.out.println(voidList.get(0));
  }
}
