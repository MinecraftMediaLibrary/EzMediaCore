/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackFormatException;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackIconException;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The resourcepack wrapper which can be used to wrap sound files and be hosted as a file. It has
 * the ability to modify the base resourcepack file as well as the sounds.json file. You may also
 * specify other attributes such as the icon, description, and format.
 */
public class ResourcepackWrapper implements PackHolder, ConfigurationSerializable {

  private static final Gson GSON;

  static {
    GSON = new GsonBuilder().setPrettyPrinting().create();
  }

  private final MinecraftMediaLibrary library;
  private final String path;
  private final File audio;
  private final File icon;
  private final String description;
  private final int packFormat;

  /**
   * Instantiates a new Resourcepack wrapper.
   *
   * @param library the library
   * @param path the path
   * @param audio the audio
   * @param icon the icon
   * @param description the description
   * @param packFormat the pack format
   */
  public ResourcepackWrapper(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String path,
      @NotNull final File audio,
      @Nullable final File icon,
      final String description,
      final int packFormat) {
    this.library = library;
    this.path = path;
    this.audio = audio;
    this.icon = icon;
    this.description = description;
    this.packFormat = packFormat;
    if (!ResourcepackUtilities.validatePackFormat(packFormat)) {
      throw new InvalidPackFormatException("Invalid Pack Format Exception (" + packFormat + ")");
    }
    if (icon != null && !ResourcepackUtilities.validateResourcepackIcon(icon)) {
      throw new InvalidPackIconException("Invalid Pack Icon! Must be PNG (" + icon.getName() + ")");
    }
    Logger.info("New Resourcepack (" + path + ") was Initialized");
  }

  /**
   * Deserializes ResourcepackWrapper.
   *
   * @param library the library
   * @param deserialize the deserialize
   * @return the resourcepack wrapper
   */
  public static ResourcepackWrapper deserialize(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final Map<String, Object> deserialize) {
    return new ResourcepackWrapper(
        library,
        String.valueOf(deserialize.get("path")),
        new File(String.valueOf(deserialize.get("audio"))),
        new File(String.valueOf(deserialize.get("icon"))),
        String.valueOf(deserialize.get("description")),
        NumberConversions.toInt(deserialize.get("pack-format")));
  }

  /**
   * Serializes ResourcepackWrapper
   *
   * @return map of serialized values
   */
  @Override
  public @NotNull Map<String, Object> serialize() {
    return ImmutableMap.of(
        "path", path,
        "audio", audio.getAbsolutePath(),
        "icon", icon.getAbsolutePath(),
        "description", description,
        "pack-format", packFormat);
  }

  /** Builds the resourcepack based on values. */
  @Override
  public void buildResourcePack() {
    onResourcepackBuild();
    Logger.info("Wrapping Resourcepack...");
    try {

      final File zipFile = new File(path);
      if (!zipFile.exists()) {
        if (zipFile.createNewFile()) {
          Logger.info("Created Zip File");
        }
      }

      final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
      final ZipEntry config = new ZipEntry("pack.mcmeta");
      out.putNextEntry(config);
      out.write(getPackJson().getBytes());
      out.closeEntry();

      final ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
      out.putNextEntry(sound);
      out.write(getSoundJson().getBytes());
      out.closeEntry();

      final ZipEntry soundFile = new ZipEntry("assets/minecraft/sounds/audio.ogg");
      out.putNextEntry(soundFile);
      out.write(Files.readAllBytes(Paths.get(audio.getAbsolutePath())));
      out.closeEntry();

      if (icon != null && icon.exists()) {
        final ZipEntry iconFile = new ZipEntry("pack.png");
        out.putNextEntry(iconFile);
        out.write(Files.readAllBytes(Paths.get(icon.getAbsolutePath())));
        out.closeEntry();
      }

      out.close();
      Logger.info("Finished Wrapping Resourcepack!");
    } catch (final IOException e) {
      Logger.error("There was an error while wrapping the resourcepack...");
      e.printStackTrace();
    }
  }

  /** Called when the resourcepack is being built. */
  @Override
  public void onResourcepackBuild() {}

  /**
   * Gets pack JSON.
   *
   * @return pack json
   */
  public String getPackJson() {
    final JsonObject mcmeta = new JsonObject();
    final JsonObject pack = new JsonObject();
    pack.addProperty("pack_format", packFormat);
    pack.addProperty("description", description);
    mcmeta.add("pack", pack);
    return GSON.toJson(mcmeta);
  }

  /**
   * Gets pack sound JSON.
   *
   * @return sound json
   */
  public String getSoundJson() {
    final JsonObject category = new JsonObject();
    final JsonObject type = new JsonObject();
    final JsonArray sounds = new JsonArray();
    sounds.add("audio");
    category.add("sounds", sounds);
    type.add(library.getPlugin().getName().toLowerCase(), category);
    return GSON.toJson(type);
  }

  /**
   * Gets library.
   *
   * @return the library
   */
  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  /**
   * Gets path.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets audio.
   *
   * @return the audio
   */
  public File getAudio() {
    return audio;
  }

  /**
   * Gets icon.
   *
   * @return the icon
   */
  public File getIcon() {
    return icon;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets pack format.
   *
   * @return the pack format
   */
  public int getPackFormat() {
    return packFormat;
  }

  /** The type Builder. */
  public static class Builder {

    private File audio;
    private File icon;
    private String description;
    private int packFormat;
    private String path;

    /**
     * Sets audio.
     *
     * @param audio the audio
     * @return the audio
     */
    public Builder setAudio(@NotNull final File audio) {
      this.audio = audio;
      return this;
    }

    /**
     * Sets icon.
     *
     * @param icon the icon
     * @return the icon
     */
    public Builder setIcon(@NotNull final File icon) {
      this.icon = icon;
      return this;
    }

    /**
     * Sets description.
     *
     * @param description the description
     * @return the description
     */
    public Builder setDescription(@NotNull final String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets pack format.
     *
     * @param packFormat the pack format
     * @return the pack format
     */
    public Builder setPackFormat(final int packFormat) {
      this.packFormat = packFormat;
      return this;
    }

    /**
     * Sets path.
     *
     * @param path the path
     * @return the path
     */
    public Builder setPath(@NotNull final String path) {
      this.path = path;
      return this;
    }

    /**
     * Create resourcepack hosting provider resourcepack wrapper.
     *
     * @param library the library
     * @return the resourcepack wrapper
     */
    public ResourcepackWrapper createResourcepackHostingProvider(
        final MinecraftMediaLibrary library) {
      return new ResourcepackWrapper(library, path, audio, icon, description, packFormat);
    }
  }
}
