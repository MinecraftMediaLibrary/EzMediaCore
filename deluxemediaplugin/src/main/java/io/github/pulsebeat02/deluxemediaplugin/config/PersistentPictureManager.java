/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
import io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors;
import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
import io.github.pulsebeat02.ezmediacore.image.DynamicImage;
import io.github.pulsebeat02.ezmediacore.image.Image;
import io.github.pulsebeat02.ezmediacore.image.StaticImage;
import rewrite.persistent.PersistentImageStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class PersistentPictureManager {

  private final DeluxeMediaPlugin plugin;
  private final PersistentImageStorage storage;
  private final List<Image> images;

  public PersistentPictureManager(@NotNull final DeluxeMediaPlugin plugin) throws IOException {
    this.plugin = plugin;
    this.storage = this.getPictureStorage();
    this.images = this.getInternalImages();
  }

  private @NotNull List<Image> getInternalImages() throws IOException {
    return this.storage.deserialize();
  }

  private @NotNull PersistentImageStorage getPictureStorage() {
    return new PersistentImageStorage(
        this.plugin.getBootstrap().getDataFolder().toPath().resolve("pictures.json"));
  }

  public void startTask() {
    FixedExecutors.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
        this::scheduledSave, 0, 5, TimeUnit.MINUTES);
  }

  private void scheduledSave() {
    try {
      this.save();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
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

  public void save() throws IOException {
    this.storage.serialize(this.images);
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
