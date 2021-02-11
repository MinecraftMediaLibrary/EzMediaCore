/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

public interface AbstractPackHolder {

  /** Builds the resourcepack with all the files and specified pack.mcmeta values. */
  void buildResourcePack();

  /** Called before the resourcepack starts building. */
  void onResourcepackBuild();
}
