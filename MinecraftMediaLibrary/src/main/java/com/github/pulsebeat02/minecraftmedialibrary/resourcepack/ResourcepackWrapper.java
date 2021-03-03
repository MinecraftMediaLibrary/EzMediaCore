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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackFormatException;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackIconException;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcepackWrapper implements AbstractPackHolder, ConfigurationSerializable {

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
      final File icon,
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
    final Map<String, Object> serialized = new HashMap<>();
    serialized.put("path", path);
    serialized.put("audio", audio.getAbsolutePath());
    serialized.put("icon", icon.getAbsolutePath());
    serialized.put("description", description);
    serialized.put("pack-format", packFormat);
    return serialized;
  }

  /** Builds the resourcepack based on values. */
  @Override
  public void buildResourcePack() {
    onResourcepackBuild();
    Logger.info("Wrapping Resourcepack...");
    // TODO: Fix this mess and use GSON to make the JSON file
    try {
      final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
      final byte[] mcmeta =
          ("{\r\n"
                  + "	\"pack\": {\r\n"
                  + "    \"pack_format\": "
                  + packFormat
                  + ",\r\n"
                  + "    \"description\": \""
                  + description
                  + "\"\r\n"
                  + "  }\r\n"
                  + "}")
              .getBytes();
      final ZipEntry config = new ZipEntry("pack.mcmeta");
      out.putNextEntry(config);
      out.write(mcmeta);
      out.closeEntry();
      final byte[] soundJSON =
          ("{\r\n"
                  + "   \""
                  + library.getPlugin().getName()
                  + "\":{\r\n"
                  + "      \"sounds\":[\r\n"
                  + "         \"audio\"\r\n"
                  + "      ]\r\n"
                  + "   }\r\n"
                  + "}")
              .getBytes();
      final ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
      out.putNextEntry(sound);
      out.write(soundJSON);
      out.closeEntry();
      final ZipEntry soundFile = new ZipEntry("assets/minecraft/sounds/audio.ogg");
      out.putNextEntry(soundFile);
      out.write(Files.readAllBytes(Paths.get(audio.getAbsolutePath())));
      final ZipEntry iconFile = new ZipEntry("pack.png");
      out.putNextEntry(iconFile);
      out.write(Files.readAllBytes(Paths.get(icon.getAbsolutePath())));
      out.closeEntry();
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
    public Builder setAudio(final File audio) {
      this.audio = audio;
      return this;
    }

    /**
     * Sets icon.
     *
     * @param icon the icon
     * @return the icon
     */
    public Builder setIcon(final File icon) {
      this.icon = icon;
      return this;
    }

    /**
     * Sets description.
     *
     * @param description the description
     * @return the description
     */
    public Builder setDescription(final String description) {
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
    public Builder setPath(final String path) {
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
