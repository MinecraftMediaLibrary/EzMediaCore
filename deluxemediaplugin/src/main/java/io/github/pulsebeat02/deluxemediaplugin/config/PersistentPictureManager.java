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
import org.jetbrains.annotations.NotNull;

public class PersistentPictureManager {

  private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;

  static {
    SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
  }

  private final DeluxeMediaPlugin plugin;
  private final PersistentImageStorage storage;
  private final List<Image> images;

  public PersistentPictureManager(@NotNull final DeluxeMediaPlugin plugin) throws IOException {
    this.plugin = plugin;
    this.storage =
        new PersistentImageStorage(plugin.getDataFolder().toPath().resolve("pictures.json"));
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
      this.plugin.getLogger().log(Level.SEVERE, "There was an issue saving images!");
      e.printStackTrace();
    }
  }

  public DeluxeMediaPlugin getPlugin() {
    return this.plugin;
  }

  public PersistentImageStorage getStorage() {
    return this.storage;
  }

  public List<Image> getImages() {
    return this.images;
  }
}
