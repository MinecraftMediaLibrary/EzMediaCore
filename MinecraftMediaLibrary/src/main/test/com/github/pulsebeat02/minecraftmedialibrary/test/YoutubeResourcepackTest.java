/*
 * ============================================================================
 *  Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 *  This file is part of MinecraftMediaLibrary
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *
 *  Written by Brandon Li <brandonli2006ma@gmail.com>, 2/12/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.concurrent.AsyncVideoExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImage;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YoutubeResourcepackTest extends JavaPlugin {

  private MinecraftMediaLibrary library;

  @Override
  public void onEnable() {
    library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
  }

  public String getResourcepackUrlYoutube(
      @NotNull final String youtubeUrl, @NotNull final String directory, final int port) {

    final YoutubeExtraction extraction =
        new YoutubeExtraction(
            youtubeUrl, directory, new ExtractionSetting.Builder().createExtractionSetting()) {
          @Override
          public void onVideoDownload() {
            System.out.println("Video is Downloading!");
          }

          @Override
          public void onAudioExtraction() {
            System.out.println("Audio is being extracted from Video!");
          }
        };
    final ExecutorService executor = Executors.newCachedThreadPool();
    CompletableFuture.runAsync(() -> new AsyncVideoExtraction(extraction).extractAudio(), executor);
    CompletableFuture.runAsync(
        () -> new AsyncVideoExtraction(extraction).downloadVideo(), executor);

    final ResourcepackWrapper wrapper =
        new ResourcepackWrapper.Builder()
            .setAudio(extraction.getAudio())
            .setDescription("Youtube Video: " + extraction.getVideoTitle())
            .setPath(directory)
            .setPackFormat(6)
            .createResourcepackHostingProvider(library);
    wrapper.buildResourcePack();

    final HttpDaemonProvider hosting = new HttpDaemonProvider(directory, port);
    hosting.startServer();

    return hosting.generateUrl(Paths.get(directory));
  }

  public void displayImage(final int map, @NotNull final File image) throws IOException {

    final BufferedImage bi = ImageIO.read(image);

    final MapImage imageMap =
        new MapImage.Builder()
            .setMap(map)
            .setWidth(bi.getWidth())
            .setHeight(bi.getHeight())
            .createImageMap(library);

    imageMap.drawImage();
  }
}
