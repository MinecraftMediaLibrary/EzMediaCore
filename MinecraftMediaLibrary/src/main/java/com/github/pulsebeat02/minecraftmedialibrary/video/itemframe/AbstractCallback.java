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

package com.github.pulsebeat02.minecraftmedialibrary.video.itemframe;

import org.jetbrains.annotations.NotNull;

public interface AbstractCallback {

  /**
   * Sends data for map packets to the players.
   *
   * @param data to send
   */
  void send(@NotNull final int[] data);
}
