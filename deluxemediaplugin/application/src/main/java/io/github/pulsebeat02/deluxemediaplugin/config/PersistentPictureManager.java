/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
import io.github.pulsebeat02.ezmediacore.image.DynamicImage;
import io.github.pulsebeat02.ezmediacore.image.Image;
import io.github.pulsebeat02.ezmediacore.image.StaticImage;
import io.github.pulsebeat02.ezmediacore.persistent.PersistentImageStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PersistentPictureManager {

  private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;

  static {
    SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
  }

  private final DeluxeMediaPlugin plugin;
  private final JavaPlugin loader;
  private final PersistentImageStorage storage;
  private final List<Image> images;

  public PersistentPictureManager(@NotNull final DeluxeMediaPlugin plugin) throws IOException {
    this.plugin = plugin;
    this.loader = plugin.getBootstrap();
    this.storage =
        new PersistentImageStorage(this.loader.getDataFolder().toPath().resolve("pictures.json"));
    final List<Image> images = this.storage.deserialize();
    this.images = images == null ? new ArrayList<>() : images;
  }

  public void startTask() {
    SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(this::save, 0, 5, TimeUnit.MINUTES);
  }

  public void addPhoto(
      final List<Integer> maps,
      @NotNull final Path file,
      @NotNull final ImmutableDimension dimension)
      throws IOException {
    this.images.add(new StaticImage(this.plugin.library(), file, maps, dimension));
  }

  public void addGif(
      final List<Integer> maps,
      @NotNull final Path file,
      @NotNull final ImmutableDimension dimension)
      throws IOException {
    this.images.add(new DynamicImage(this.plugin.library(), file, maps, dimension));
  }

  public void save() {
    try {
      this.storage.serialize(this.images);
    } catch (final IOException e) {
      this.loader.getLogger().log(Level.SEVERE, "There was an issue saving images!");
      e.printStackTrace();
    }
  }

  public @NotNull DeluxeMediaPlugin getPlugin() {
    return this.plugin;
  }

  public @NotNull PersistentImageStorage getStorage() {
    return this.storage;
  }

  public @NotNull List<Image> getImages() {
    return this.images;
  }
}
