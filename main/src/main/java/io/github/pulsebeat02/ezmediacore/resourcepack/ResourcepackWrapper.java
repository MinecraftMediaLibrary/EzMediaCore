package io.github.pulsebeat02.ezmediacore.resourcepack;

import com.google.gson.JsonObject;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidPackFormatException;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidPackResourceException;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourcepackWrapper implements PackWrapper {

  private final Map<String, byte[]> files;
  private final Path path;
  private final String description;
  private final int format;
  private final Path icon;

  public ResourcepackWrapper(
      @NotNull final Path path, @NotNull final String description, final int format) {
    this(path, description, format, null);
  }

  public ResourcepackWrapper(
      @NotNull final Path path,
      @NotNull final String description,
      final int format,
      @Nullable final Path icon) {
    this.files = new HashMap<>();
    this.path = path;
    this.description = description;
    this.format = format;
    this.icon = icon;
    if (!ResourcepackUtils.validatePackFormat(format)) {
      throw new InvalidPackFormatException(format);
    }
    if (icon != null && !ResourcepackUtils.validateResourcepackIcon(icon)) {
      throw new InvalidPackResourceException(
          "Invalid Pack Icon! Must be PNG (%s)".formatted(PathUtils.getName(icon)));
    }
  }

  @Override
  public void wrap() throws IOException {
    this.onPackStartWrap();
    this.internalWrap();

    //    try (final ZipOutputStream out =
    //        new ZipOutputStream(new FileOutputStream(this.path.toFile()))) {
    //
    //      FileUtils.createFileIfNotExists(this.path);
    //
    //      final ZipEntry config = new ZipEntry("pack.mcmeta");
    //      out.putNextEntry(config);
    //      out.write(getPackMcmeta().getBytes());
    //      out.closeEntry();
    //
    //      final ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
    //      out.putNextEntry(sound);
    //      out.write(getSoundJson().getBytes());
    //      out.closeEntry();
    //
    //      final ZipEntry soundFile = new ZipEntry("assets/minecraft/sounds/audio.ogg");
    //      out.putNextEntry(soundFile);
    //      out.write(Files.readAllBytes(Paths.get(this.audio.toString())));
    //      out.closeEntry();
    //
    //      if (this.icon != null && Files.exists(this.icon)) {
    //        final ZipEntry iconFile = new ZipEntry("pack.png");
    //        out.putNextEntry(iconFile);
    //        out.write(Files.readAllBytes(icon));
    //        out.closeEntry();
    //      }
    //    }

    this.onPackFinishWrap();
  }

  @Override
  public void internalWrap() throws IOException {

    Logger.error("Wrapping the Resourcepack");
    try (final ZipOutputStream out =
        new ZipOutputStream(new FileOutputStream(this.path.toFile()))) {

      FileUtils.createIfNotExists(this.path);
      this.addFile("pack.mcmeta", this.getPackMcmeta().getBytes());

      if (this.icon != null) {
        this.addFile("pack.png", this.icon);
      }

      for (final Map.Entry<String, byte[]> entry : this.files.entrySet()) {
        out.putNextEntry(new ZipEntry(entry.getKey()));
        out.write(entry.getValue());
        out.closeEntry();
      }
    }
    Logger.info("Finished Wrapping Resourcepack");
  }

  @Override
  public void onPackStartWrap() {
  }

  @Override
  public void onPackFinishWrap() {
  }

  @Override
  public void addFile(@NotNull final String path, @NotNull final Path file) throws IOException {
    this.files.put(path, Files.readAllBytes(file));
  }

  @Override
  public void addFile(@NotNull final String path, final byte[] file) {
    this.files.put(path, file);
  }

  @Override
  public void removeFile(@NotNull final String path) {
    this.files.remove(path);
  }

  @Override
  public @NotNull Map<String, byte[]> listFiles() {
    return this.files;
  }

  @Override
  public @NotNull Path getResourcepackFilePath() {
    return this.path;
  }

  @Override
  public @NotNull Path getIconPath() {
    return this.icon;
  }

  @Override
  public @NotNull String getDescription() {
    return this.description;
  }

  @Override
  public @NotNull String getPackMcmeta() {
    final JsonObject mcmeta = new JsonObject();
    final JsonObject pack = new JsonObject();
    pack.addProperty("pack_format", this.format);
    pack.addProperty("description", this.description);
    mcmeta.add("pack", pack);
    return GsonProvider.getSimple().toJson(mcmeta);
  }

  @Override
  public int getPackFormat() {
    return this.format;
  }
}
