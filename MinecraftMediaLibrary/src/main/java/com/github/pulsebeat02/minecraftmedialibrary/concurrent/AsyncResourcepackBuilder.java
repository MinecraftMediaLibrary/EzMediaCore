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

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.AbstractPackHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncResourcepackBuilder {

  private final AbstractPackHolder packHolder;

  public AsyncResourcepackBuilder(@NotNull final AbstractPackHolder packHolder) {
    this.packHolder = packHolder;
  }

  public CompletableFuture<Void> buildResourcePack() {
    return CompletableFuture.runAsync(packHolder::buildResourcePack);
  }
}
