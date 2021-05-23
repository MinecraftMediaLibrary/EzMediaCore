package com.github.pulsebeat02.minecraftmedialibrary.vlc.os;

import uk.co.caprica.vlcj.binding.RuntimeUtil;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VLCUtilities;
import com.sun.jna.NativeLibrary;

public abstract class AbstractSilentOSDependentSolution implements SilentOSDependentSolution {

  private final Path dir;

  /**
   * Instantiates a new SilentOSDependentSolution.
   *
   * @param library the library
   */
  protected AbstractSilentOSDependentSolution(@NotNull final MediaLibrary library) {
    this(library.getVlcFolder());
  }

  /**
   * Instantiates a new SilentOSDependentSolution.
   *
   * @param dir the directory
   */
  protected AbstractSilentOSDependentSolution(@NotNull final Path dir) {
    this.dir = dir;
  }

  /**
   * Loads native dependency from file.
   *
   * @param folder directory
   */
  @Override
  public void loadNativeDependency(@NotNull final File folder) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), folder.getAbsolutePath());
    VLCUtilities.checkVLCExistence(folder);
  }

  /** Prints all System environment variables. */
  @Override
  public void printSystemEnvironmentVariables() {
    Logger.info("======== SYSTEM ENVIRONMENT VARIABLES ========");
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      Logger.info(String.format("Key: %s | Entry: %s", entry.getKey(), entry.getValue()));
    }
    Logger.info("==============================================");
  }

  /** Prints all System properties. */
  @Override
  public void printSystemProperties() {
    Logger.info("============== SYSTEM PROPERTIES ==============");
    final Properties p = System.getProperties();
    final Enumeration<Object> keys = p.keys();
    while (keys.hasMoreElements()) {
      final String key = (String) keys.nextElement();
      Logger.info(String.format("Key: %s | Entry: %s", key, p.get(key)));
    }
    Logger.info("===============================================");
  }

  /**
   * Gets VLC folder in folder.
   *
   * @param folder search folder
   * @return file
   */
  @Override
  @Nullable
  public File findVLCFolder(@NotNull final File folder) {
    for (final File f : Objects.requireNonNull(folder.listFiles())) {
      final String name = f.getName();
      if (StringUtils.containsIgnoreCase(name, "vlc") && !name.endsWith(".dmg")) {
        return f;
      }
    }
    return null;
  }

  /**
   * Deletes file (archive).
   *
   * @param zip archive
   */
  @Override
  public void deleteArchive(@NotNull final File zip) {
    Logger.info("Deleting Archive...");
    if (zip.delete()) {
      Logger.info("Archive deleted after installation.");
    } else {
      Logger.error("Archive could NOT be deleted after installation!");
    }
  }

  @Override
  public Path getDir() {
    return dir;
  }
}
