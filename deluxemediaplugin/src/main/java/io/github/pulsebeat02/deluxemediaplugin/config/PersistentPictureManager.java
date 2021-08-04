package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.ezmediacore.image.DynamicImage;
import io.github.pulsebeat02.ezmediacore.image.Image;
import io.github.pulsebeat02.ezmediacore.image.StaticImage;
import io.github.pulsebeat02.ezmediacore.persistent.PersistentImageStorage;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class PersistentPictureManager {

  private final DeluxeMediaPlugin plugin;
  private final PersistentImageStorage storage;
  private final List<Image> images;

  public PersistentPictureManager(@NotNull final DeluxeMediaPlugin plugin) throws IOException {
    this.plugin = plugin;
    this.storage =
        new PersistentImageStorage(plugin.getDataFolder().toPath().resolve("pictures.yml"));
    this.images = this.storage.deserialize();
  }

  public void startTask() {
    new BukkitRunnable() {
      @Override
      public void run() {
        PersistentPictureManager.this.save();
      }
    }.runTaskTimerAsynchronously(this.plugin, 0L, 6000L);
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
