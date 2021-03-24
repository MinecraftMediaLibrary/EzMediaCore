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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

/**
 * The main pack holder base which can be used to create other types of pack holders.
 * MinecraftMediaLibrary uses this interface for pack holders.
 */
public interface PackHolder {

  /** Builds the resourcepack with all the files and specified pack.mcmeta values. */
  void buildResourcePack();

  /** Called before the resourcepack starts building. */
  void onResourcepackBuild();
}
