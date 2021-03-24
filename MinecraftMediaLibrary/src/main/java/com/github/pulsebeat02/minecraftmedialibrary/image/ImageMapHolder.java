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

package com.github.pulsebeat02.minecraftmedialibrary.image;

/**
 * Holds the map image and functions which call the necessary draw functions and events when
 * necessary. Also can be used to create custom map image classes. Used within the
 * MinecraftMediaLibrary to draw/display images to players.
 */
public interface ImageMapHolder {

  /** Draws a specified image onto a Map. */
  void drawImage();

  /** Called before image is drawn. */
  void onDrawImage();
}
