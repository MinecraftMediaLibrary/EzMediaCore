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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.exception.UnsupportedOperatingSystemException;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourceUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The main package fetcher for Linux Distributions. It parses a JSON file of all the packaged
 * binaries from mirror sites called "linux-package-installation.json", which can be found directly
 * in the root folder of the JAR (or the resources folder). After parsing, it properly tries to
 * track the right package for the current distribution. If the package mirror is down, it will
 * automatically resort to a Github hosted repository of binaries (hosted by me) which contains all
 * the binaries instead.
 */
@SuppressWarnings("UnstableApiUsage")
public class LinuxPackageManager {

  private static final TypeToken<Map<String, List<LinuxPackage>>>
      MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN;
  private static final TypeToken<Map<String, LinuxOSPackages>>
      MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN;
  private static final Gson GSON;
  private static LinuxPackage PACKAGE;

  static {
    MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN = new TypeToken<Map<String, List<LinuxPackage>>>() {};
    MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN = new TypeToken<Map<String, LinuxOSPackages>>() {};
    GSON =
        new GsonBuilder()
            .registerTypeAdapter(LinuxOSPackages.class, new LinuxOSPackagesAdapter())
            .setPrettyPrinting()
            .create();
  }

  private final File vlc;
  private Map<String, LinuxOSPackages> packages;

  /**
   * Instantiates a LinuxPackageManager.
   *
   * @param library instance
   */
  public LinuxPackageManager(@NotNull final MinecraftMediaLibrary library) {
    this(library.getVlcFolder());
  }

  /**
   * Instantiates a LinuxPackageManager.
   *
   * @param dir directory
   */
  public LinuxPackageManager(@NotNull final String dir) {
    Logger.info("Reading System OS JSON file...");
    try {
      packages =
          GSON.fromJson(
              ResourceUtilities.getFileContents("linux-package-installation.json"),
              MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN.getType());
      Logger.info("Successfully read System OS JSON file");
    } catch (final IOException e) {
      Logger.info("Could not read System OS JSON file");
      e.printStackTrace();
    }
    vlc = new File(dir);
    if (!vlc.exists()) {
      if (vlc.mkdir()) {
        Logger.info("Made VLC Directory");
      } else {
        Logger.error("Failed to Make VLC Directory");
      }
    }
  }

  public static LinuxPackage getPackage() {
    return PACKAGE;
  }

  /**
   * Gets package for current Operating System.
   *
   * @return package stored in archive
   * @throws IOException exception if file can't be downloaded
   */
  @NotNull
  public File getDesignatedPackage() throws IOException {
    Logger.info("Attempting to Find VLC Package for Machine.");
    final String fullInfo = RuntimeUtilities.getLinuxDistribution();
    final String distro = RuntimeUtilities.getDistributionName(fullInfo).toLowerCase();
    final String ver = RuntimeUtilities.getDistributionVersion(fullInfo).toLowerCase();
    List<LinuxPackage> set = null;
    outer:
    for (final Map.Entry<String, LinuxOSPackages> entry : packages.entrySet()) {
      final String name = entry.getKey().toLowerCase();
      Logger.info("Attempting Operating System " + name);
      if (distro.contains(name)) {
        Logger.info("Found Operating System: " + name);
        final ListMultimap<String, LinuxPackage> links = entry.getValue().getLinks();
        for (final String version : links.keySet()) {
          Logger.info("Attempting Version: " + version);
          if (ver.contains(version.toLowerCase())) {
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
    final CPUArchitecture arch =
        CPUArchitecture.fromName(RuntimeUtilities.getCpuArch().toUpperCase());
    if (set == null || arch == null) {
      Logger.error("Could not find architecture... throwing an error!");
      throw new UnsupportedOperatingSystemException("Unsupported Operating System Platform!");
    }
    for (final LinuxPackage link : set) {
      Logger.info("Trying Out Link: " + link.getUrl());
      if (link.getArch() == arch) {
        final String url = link.getUrl();
        final String fileName = url.substring(url.lastIndexOf("/") + 1);
        final File file = new File(vlc.getAbsolutePath() + "/" + fileName);
        URL uri = null;
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
        if (uri != null) {
          FileUtils.copyURLToFile(uri, file);
          PACKAGE = link;
          return file;
        }
      }
    }
    Logger.error("Could not find architecture... throwing an error!");
    throw new UnsupportedOperatingSystemException("Unsupported Operating System Platform!");
  }

  /** Extract contents. Should only be one package located in folder */
  public void extractContents() {
    ArchiveUtilities.recursiveExtraction(vlc.listFiles()[0], vlc);
  }

  /**
   * Gets packages.
   *
   * @return all packages
   */
  public Map<String, LinuxOSPackages> getAllPackages() {
    return packages;
  }

  private static final class LinuxOSPackagesAdapter extends TypeAdapter<LinuxOSPackages> {

    /**
     * Write method for JSON.
     *
     * @param out writer
     * @param linuxOSPackages packages
     */
    @Override
    public void write(final JsonWriter out, final LinuxOSPackages linuxOSPackages) {
      GSON.toJson(
          linuxOSPackages.getLinks().asMap(),
          MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN.getType(),
          out);
    }

    /**
     * Read method for JSON.
     *
     * @param in reader
     * @return result
     */
    @Override
    public LinuxOSPackages read(final JsonReader in) {
      final Map<String, Collection<LinuxPackage>> map =
          GSON.fromJson(in, MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN.getType());
      final ListMultimap<String, LinuxPackage> multimap = ArrayListMultimap.create();
      map.forEach(multimap::putAll);
      return new LinuxOSPackages(multimap);
    }
  }
}
