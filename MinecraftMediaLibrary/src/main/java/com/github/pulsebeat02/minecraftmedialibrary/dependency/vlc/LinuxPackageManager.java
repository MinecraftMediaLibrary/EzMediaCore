/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/26/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.exception.UnsupportedOperatingSystemException;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.OperatingSystemUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ZipFileUtilities;
import com.google.common.collect.HashMultimap;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class LinuxPackageManager {

  private Map<String, LinuxOSPackages> packages;

  /**
   * Instantiates a new Linux package manager.
   */
  public LinuxPackageManager() {
    try {
      final Type token = new TypeToken<HashMultimap<String, LinuxPackage>>() {
      }.getType();
      packages = new Gson().fromJson(getFileContents(), token);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets package for current Operating System.
   *
   * @return package stored in archive
   * @throws IOException exception if file can't be downloaded
   */
  public File getPackage() throws IOException {
    Logger.info("Attempting to Find VLC Package for Machine.");
    final String os = OperatingSystemUtilities.OPERATING_SYSTEM;
    Set<LinuxPackage> set = null;
    outer:
    for (final Map.Entry<String, LinuxOSPackages> entry : packages.entrySet()) {
      final String name = entry.getKey().toLowerCase();
      Logger.info("Attempting Operating System" + name);
      if (os.contains(name)) {
        Logger.info("Found Operating System: " + name);
        final HashMultimap<String, LinuxPackage> links = entry.getValue().getLinks();
        for (final String version : links.keySet()) {
          Logger.info("Attempting Version: " + version);
          if (os.contains(version.toLowerCase())) {
            Logger.info("Found Version: " + version);
            set = links.get(version);
            break outer;
          }
        }
        Logger.warn("Could not find version, resorting to LATEST.");
        set = links.get("LATEST");
        break;
      }
    }
    final CPUArchitecture arch = CPUArchitecture.fromName(OperatingSystemUtilities.CPU_ARCH);
    if (set == null || arch == null) {
      Logger.error("Could not find architecture... throwing an error!");
      throw new UnsupportedOperatingSystemException("Unsupported Operating System Platform!");
    }
    final File vlc = new File("/vlc");
    if (!vlc.exists()) {
      if (vlc.mkdir()) {
        Logger.info("Made VLC Directory");
      } else {
        Logger.error("Failed to Make VLC Directory");
      }
    }
    for (final LinuxPackage link : set) {
      Logger.info("Trying Out Link: " + link);
      if (link.getArch() == arch) {
        final String url = link.getUrl();
        final String fileName = url.substring(url.lastIndexOf("/") + 1);
        final File file = new File("/vlc/" + fileName);
        URL uri = new URL("");
        try {
          uri = new URL(link.getUrl());
        } catch (final MalformedURLException e) {
          Logger.info("Main Site is Down! Using Mirror! (" + url + ")");
          try {
            uri = new URL(link.getMirror());
          } catch (final MalformedURLException e1) {
            Logger.error("Github Mirror is Down. You living in 2140 or something?");
            e1.printStackTrace();
          }
          e.printStackTrace();
        }
        FileUtils.copyURLToFile(uri, file);
        return file;
      }
    }
    Logger.error("Could not find architecture... throwing an error!");
    throw new UnsupportedOperatingSystemException("Unsupported Operating System Platform!");
  }

  /**
   * Extract contents.
   */
  public void extractContents() {
    final File vlc = new File("/vlc");
    final File f = new File("/vlc").listFiles()[0];
    final String name = f.getName();
    Logger.info("Trying to find extension for file: " + name);
    if (name.endsWith("deb") || name.endsWith("rpm") || name.endsWith("eopkg")) {
      Logger.info("Found .deb, .rpm, or .eopkg File!");
      ZipFileUtilities.decompressArchive(f, vlc);
    } else if (name.endsWith("txz") || name.endsWith(".tar.xz")) {
      Logger.info("Found .txz or .tar.xz File!");
      ZipFileUtilities.decompressArchive(f, vlc, "tar", "xz");
    } else if (name.endsWith("tgz")) {
      Logger.info("Found .tgz File!");
      ZipFileUtilities.decompressArchive(f, vlc, "tar", "gz");
    } else if (name.endsWith(".tar.zst")) {
      Logger.warn(
              "Hello user, please read this error carefully: Your computer seems to be using "
                      + "KAOS Linux. The extract for KAOS Linux is a .tar.zst file, which is yet not supported by "
                      + "the plugin yet. The archive has been downloaded in the /vlcj folder, and it is required by "
                      + "you to extract the file in order to get the VLC libraries. This is a required step, and VLCJ "
                      + "will not run if you do not perform this step.");
    }
  }

  /**
   * Gets resource from JSON file in resources folder.
   *
   * @return contents in String format
   * @throws IOException if file couldn't be found
   */
  private String getFileContents() throws IOException {
    final String name = "linux-package-installation.json";
    final ClassLoader loader = getClass().getClassLoader();
    final InputStream input = loader.getResourceAsStream(name);
    if (input == null) {
      throw new IllegalArgumentException("file not found! " + name);
    } else {
      return IOUtils.toString(input, StandardCharsets.UTF_8.name());
    }
  }

  /**
   * Gets packages.
   *
   * @return the packages
   */
  public Map<String, LinuxOSPackages> getAllPackages() {
    return packages;
  }
}
