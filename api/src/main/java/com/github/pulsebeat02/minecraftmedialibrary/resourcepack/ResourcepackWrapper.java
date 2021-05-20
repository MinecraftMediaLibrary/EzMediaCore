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

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackFormatException;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackIconException;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.json.GsonHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The resourcepack wrapper which can be used to wrap sound files and be hosted as a file. It has
 * the ability to modify the base resourcepack file as well as the sounds.json file. You may also
 * specify other attributes such as the icon, description, and format.
 */
public class ResourcepackWrapper implements PackHolder, ConfigurationSerializable {

  private final String path;
  private final Path audio;
  private final String soundName;
  private final Path icon;
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
      @NotNull final MediaLibrary library,
      @NotNull final String path,
      @NotNull final Path audio,
      @Nullable final Path icon,
      @NotNull final String description,
      final int packFormat) {
    this(library.getPlugin().getName().toLowerCase(), path, audio, icon, description, packFormat);
  }

  /**
   * Instantiates a new Resourcepack wrapper.
   *
   * @param name the sound name
   * @param path the path
   * @param audio the audio
   * @param icon the icon
   * @param description the description
   * @param packFormat the pack format
   */
  public ResourcepackWrapper(
      @NotNull final String name,
      @NotNull final String path,
      @NotNull final Path audio,
      @Nullable final Path icon,
      @NotNull final String description,
      final int packFormat) {
    this.soundName = name;
    this.path = path;
    this.audio = audio;
    this.icon = icon;
    this.description = description;
    this.packFormat = packFormat;
    if (!ResourcepackUtilities.validatePackFormat(packFormat)) {
      throw new InvalidPackFormatException(
          String.format("Invalid Pack Format Exception (%d)", packFormat));
    }
    if (icon != null && !ResourcepackUtilities.validateResourcepackIcon(icon)) {
      throw new InvalidPackIconException(
          String.format("Invalid Pack Icon! Must be PNG (%s)", icon.getFileName().toString()));
    }
    Logger.info(String.format("New Resourcepack (%s) was Initialized", path));
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a ResourcepackWrapper out of a YoutubeExtractor.
   *
   * @param extractor the YoutubeExtractor
   * @param library the library
   * @return the resulting ResourcepackWrapper
   */
  public static ResourcepackWrapper of(
      @NotNull final MediaLibrary library, @NotNull final YoutubeExtraction extractor) {
    return ResourcepackWrapper.builder()
        .setAudio(extractor.getAudio())
        .setDescription(String.format("Youtube Video: %s", extractor.getVideoTitle()))
        .setPath(
            String.format(
                "%s/mml/http/resourcepack.zip",
                library.getPlugin().getDataFolder().getAbsolutePath()))
        .setPackFormat(6)
        .build(library);
  }

  /**
   * Creates a ResourcepackWrapper out of a File.
   *
   * @param audio the audio file
   * @param library the library
   * @return the resulting ResourcepackWrapper
   */
  public static ResourcepackWrapper of(
      @NotNull final MediaLibrary library, @NotNull final Path audio) {
    return ResourcepackWrapper.builder()
        .setAudio(audio)
        .setDescription(String.format("Media: %s", audio.getFileName().toString()))
        .setPath(
            String.format(
                "%s/mml/http/resourcepack.zip",
                library.getPlugin().getDataFolder().getAbsolutePath()))
        .setPackFormat(6)
        .build(library);
  }

  /**
   * Deserializes ResourcepackWrapper.
   *
   * @param library the library
   * @param deserialize the deserialize
   * @return the resourcepack wrapper
   */
  @NotNull
  public static ResourcepackWrapper deserialize(
      @NotNull final MediaLibrary library,
      @NotNull final Map<String, Object> deserialize) {
    return new ResourcepackWrapper(
        library,
        String.valueOf(deserialize.get("path")),
        Paths.get(String.valueOf(deserialize.get("audio"))),
        Paths.get(String.valueOf(deserialize.get("icon"))),
        String.valueOf(deserialize.get("description")),
        NumberConversions.toInt(deserialize.get("pack-format")));
  }

  /**
   * Serializes ResourcepackWrapper
   *
   * @return map of serialized values
   */
  @Override
  @NotNull
  public Map<String, Object> serialize() {
    return ImmutableMap.of(
        "path", path,
        "audio", audio.toAbsolutePath().toString(),
        "icon", icon.toAbsolutePath().toString(),
        "description", description,
        "pack-format", packFormat);
  }

  /** Builds the resourcepack based on values. */
  @Override
  public void buildResourcePack() {
    onResourcepackBuild();
    Logger.info("Wrapping Resourcepack...");
    try {

      final Path zipFile = Paths.get(path);
      if (!Files.exists(zipFile)) {
        Files.createFile(zipFile);
      }

      final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile.toFile()));
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
      out.write(Files.readAllBytes(Paths.get(audio.toAbsolutePath().toString())));
      out.closeEntry();

      if (icon != null && Files.exists(icon)) {
        final ZipEntry iconFile = new ZipEntry("pack.png");
        out.putNextEntry(iconFile);
        out.write(Files.readAllBytes(Paths.get(icon.toAbsolutePath().toString())));
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
  @NotNull
  private String getPackJson() {
    final JsonObject mcmeta = new JsonObject();
    final JsonObject pack = new JsonObject();
    pack.addProperty("pack_format", packFormat);
    pack.addProperty("description", description);
    mcmeta.add("pack", pack);
    return GsonHandler.getGson().toJson(mcmeta);
  }

  /**
   * Gets pack sound JSON.
   *
   * @return sound json
   */
  @NotNull
  private String getSoundJson() {
    final JsonObject category = new JsonObject();
    final JsonObject type = new JsonObject();
    final JsonArray sounds = new JsonArray();
    sounds.add("audio");
    category.add("sounds", sounds);
    type.add(soundName, category);
    return GsonHandler.getGson().toJson(type);
  }

  /**
   * Checks if the two ResourcepackWrapper objects are equal.
   *
   * @param obj the other object
   * @return whether the two objects are equal or not
   */
  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ResourcepackWrapper)) {
      return false;
    }
    final ResourcepackWrapper wrapper = (ResourcepackWrapper) obj;
    return path.equals(wrapper.getPath())
        && audio.equals(wrapper.getAudio())
        && icon.equals(wrapper.getIcon())
        && description.equals(wrapper.getDescription())
        && packFormat == wrapper.getPackFormat();
  }

  /**
   * Returns a String version of the current instance.
   *
   * @return the stringified version of the instance
   */
  @Override
  public String toString() {
    return GsonHandler.getGson().toJson(this);
  }

  /**
   * Gets sound name.
   *
   * @return the sound name
   */
  public String getSoundName() {
    return soundName;
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
  public Path getAudio() {
    return audio;
  }

  /**
   * Gets icon.
   *
   * @return the icon
   */
  public Path getIcon() {
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

    private Path audio;
    private Path icon;
    private String description;
    private int packFormat;
    private String path;

    private Builder() {}

    /**
     * Sets audio.
     *
     * @param audio the audio
     * @return the audio
     */
    public Builder setAudio(@NotNull final Path audio) {
      this.audio = audio;
      return this;
    }

    /**
     * Sets icon.
     *
     * @param icon the icon
     * @return the icon
     */
    public Builder setIcon(@NotNull final Path icon) {
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
    public ResourcepackWrapper build(final MediaLibrary library) {
      return new ResourcepackWrapper(library, path, audio, icon, description, packFormat);
    }

    /**
     * Create resourcepack hosting provider resourcepack wrapper (with sound).
     *
     * @param sound the sound
     * @return the resourcepack wrapper
     */
    public ResourcepackWrapper build(final String sound) {
      return new ResourcepackWrapper(sound, path, audio, icon, description, packFormat);
    }
  }
}
