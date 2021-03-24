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

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.extractor.VideoExtractorBase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * An async helper class used for extracting videos.F
 */
public class AsyncVideoExtraction {

  private final VideoExtractorBase extractor;

  /**
   * Instantiates a new AsyncVideoExtraction.
   *
   * @param extractor the extractor
   */
  public AsyncVideoExtraction(@NotNull final VideoExtractorBase extractor) {
    this.extractor = extractor;
  }

  /**
   * Download a video using CompletableFuture.
   *
   * @return the completable future
   */
  public CompletableFuture<File> downloadVideo() {
    return CompletableFuture.supplyAsync(extractor::downloadVideo);
  }

  /**
   * Extract audio using CompletableFuture.
   *
   * @return the completable future
   */
  public CompletableFuture<File> extractAudio() {
    return CompletableFuture.supplyAsync(extractor::extractAudio);
  }
}
