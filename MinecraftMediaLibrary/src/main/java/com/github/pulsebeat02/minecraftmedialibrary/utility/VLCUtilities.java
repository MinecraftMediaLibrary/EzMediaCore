package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.sun.jna.NativeLibrary;
import com.sun.jna.StringArray;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_new;
import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_release;

public final class VLCUtilities {

  private static final String VLC_PLUGIN_PATH;
  private static File NATIVE_VLC_PATH;

  static {
    VLC_PLUGIN_PATH = "VLC_PLUGIN_PATH";
  }

  private VLCUtilities() {}

  /**
   * Checks if VLC installation exists or not.
   *
   * @param library the library
   * @return whether vlc can be found or not
   */
  public static boolean checkVLCExistance(@NotNull final MinecraftMediaLibrary library) {
    String keyword = "libvlc.";
    if (RuntimeUtilities.isWindows()) {
      keyword += "dll";
    } else if (RuntimeUtilities.isMac()) {
      keyword += "dylib";
    } else if (RuntimeUtilities.isLinux()) {
      keyword += "so";
    }
    final Queue<File> folders = new ArrayDeque<>();
    folders.add(new File(library.getPlugin().getDataFolder(), "vlc"));
    while (!folders.isEmpty()) {
      final File f = folders.remove();
      if (f.isDirectory()) {
        final File[] children = f.listFiles();
        if (children != null) {
          folders.addAll(Arrays.asList(children));
        }
      } else {
        if (f.getName().equals(keyword)) {
          Logger.info("Found VLC Installation on this Server! Good Job :)");
          NATIVE_VLC_PATH = f.getParentFile();
          final String path = NATIVE_VLC_PATH.getAbsolutePath();
          NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), path);
          setVLCPluginPath(String.format("%s/plugins/", path));
          loadLibVLCLibrary();
          return true;
        }
      }
    }
    return new NativeDiscovery().discover();
  }

  /**
   * Sets the VLC plugin path to the specified path provided.
   *
   * @param path the vlc plugin path
   * @return whether the path set was successful or not
   */
  public static boolean setVLCPluginPath(@NotNull final String path) {
    if (RuntimeUtilities.isWindows()) {
      return LibC.INSTANCE._putenv(String.format("%s=%s", VLC_PLUGIN_PATH, path)) == 0;
    }
    return LibC.INSTANCE.setenv(VLC_PLUGIN_PATH, path, 1) == 0;
  }

  /**
   * Loads the LibVLC library of VLC.
   *
   * @return whether if the library was successfully loaded or not.
   */
  public static boolean loadLibVLCLibrary() {
    try {
      final libvlc_instance_t instance = libvlc_new(0, new StringArray(new String[0]));
      if (instance != null) {
        libvlc_release(instance);
        final LibVlcVersion version = new LibVlcVersion();
        if (version.isSupported()) {
          return true;
        }
      }
    } catch (final UnsatisfiedLinkError e) {
      Logger.info(e.getMessage());
    }
    return false;
  }

  /**
   * Gets the Native path of VLC binaries.
   *
   * @return the File of the folder
   */
  public static File getNativeVlcPath() {
    return NATIVE_VLC_PATH;
  }
}
