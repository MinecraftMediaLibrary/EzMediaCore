package com.github.pulsebeat02.minecraftmedialibrary.utility;

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
   * @param directory the library
   * @return whether vlc can be found or not
   */
  public static boolean checkVLCExistence(@NotNull final File directory) {
    if (new NativeDiscovery().discover()) {
      return true;
    }
    if (!directory.exists()) {
      return false;
    }
    String keyword = "libvlc.";
    if (RuntimeUtilities.isWindows()) {
      keyword += "dll";
    } else if (RuntimeUtilities.isMac()) {
      keyword += "dylib";
    } else if (RuntimeUtilities.isLinux()) {
      keyword += "so";
    }
    boolean plugins = false;
    boolean libvlc = false;
    final Queue<File> folders = new ArrayDeque<>();
    folders.add(directory);
    while (!folders.isEmpty()) {
      if (plugins && libvlc) {
        return true;
      }
      final File f = folders.remove();
      final String name = f.getName();
      if (f.isDirectory()) {
        if (!plugins && name.equals("plugins")) {
          final String path = f.getAbsolutePath();
          setVLCPluginPath(path);
          Logger.info(String.format("Found Plugins Path (%s)", path));
          plugins = true;
        } else {
          final File[] children = f.listFiles();
          if (children != null) {
            folders.addAll(Arrays.asList(children));
          }
        }
      } else {
        if (!libvlc && name.equals(keyword)) {

          /*

          In general, when we find the LibVLC file we need to also find the parent
          directory where all the binaries are stored. Unfortunately, this is different
          for every operating system out there so we must be careful.

          TODO: Verify Linux's LibVLC path is correct

           */

          if (RuntimeUtilities.isWindows()) {
            NATIVE_VLC_PATH = f.getParentFile();
          } else if (RuntimeUtilities.isMac() || RuntimeUtilities.isLinux()) {
            NATIVE_VLC_PATH = f.getParentFile().getParentFile();
          }

          Logger.info(String.format("Found LibVLC (%s)", f.getAbsoluteFile()));
          NativeLibrary.addSearchPath(
              RuntimeUtil.getLibVlcLibraryName(), NATIVE_VLC_PATH.getAbsolutePath());
          loadLibVLCLibrary();
          libvlc = true;
        }
      }
    }
    return false;
  }

  /**
   * Sets the VLC plugin path to the specified path provided.
   *
   * @param path the vlc plugin path
   * @return whether the path set was successful or not
   */
  private static boolean setVLCPluginPath(@NotNull final String path) {
    return RuntimeUtilities.isWindows()
        ? LibC.INSTANCE._putenv(String.format("%s=%s", VLC_PLUGIN_PATH, path)) == 0
        : LibC.INSTANCE.setenv(VLC_PLUGIN_PATH, path, 1) == 0;
  }

  /**
   * Loads the LibVLC library of VLC.
   *
   * @return whether if the library was successfully loaded or not.
   */
  private static boolean loadLibVLCLibrary() {
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
