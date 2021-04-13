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

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.annotation.LegacyApi;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyResolution;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.RepositoryDependency;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.sun.jna.NativeLibrary;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.LongConsumer;

/**
 * Special dependency utilities used throughout the library and also open to users. Used for easier
 * dependency management.
 */
public final class DependencyUtilities {

  private static URLClassLoader CLASSLOADER;
  private static Method ADD_URL_METHOD;
  private static File NATIVE_VLC_PATH;

  static {
    Logger.info("Attempting to Open Reflection Module...");
    try {
      final Class<?> moduleClass = Class.forName("java.lang.Module");
      final Method getModuleMethod = Class.class.getMethod("getModule");
      final Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);
      final Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
      final Object thisModule = getModuleMethod.invoke(DependencyUtilities.class);
      addOpensMethod.invoke(
          urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
      Logger.info(
          "User is using Java 9+, meaning Reflection Module does have to be opened. You may safely ignore this.");
    } catch (final ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException ignored) {
      Logger.info(
          "User is using Java 8, meaning Reflection Module does NOT have to be opened. You may safely ignore this.");
    }
    try {
      ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      ADD_URL_METHOD.setAccessible(true);
    } catch (final NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  private DependencyUtilities() {}

  /**
   * Download Maven Dependency.
   *
   * @param dependency the dependency
   * @param parent the parent
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadMavenDependency(
      @NotNull final RepositoryDependency dependency, @NotNull final String parent)
      throws IOException {
    return downloadFile(dependency, getRepoUrl(dependency), parent);
  }

  /**
   * Download Jitpack Dependency.
   *
   * @param dependency the dependency
   * @param parent the parent
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadJitpackDependency(
      @NotNull final RepositoryDependency dependency, @NotNull final String parent)
      throws IOException {
    return downloadFile(dependency, getRepoUrl(dependency), parent);
  }

  /**
   * Loads and downloads a dependency based from arguments.
   *
   * @param groupId group id
   * @param artifactId artifact id
   * @param version version
   * @param directory directory
   * @param resolution resolution
   * @return jar file
   * @throws IOException if the url constructed cannot be found
   */
  @NotNull
  public static File downloadAndLoadDependency(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String directory,
      @NotNull final DependencyResolution resolution)
      throws IOException {
    final File f = downloadFile(groupId, artifactId, version, directory, resolution);
    loadDependency(f);
    return f;
  }

  /**
   * Loads and downloads a dependency based from arguments with consumer.
   *
   * @param groupId group id
   * @param artifactId artifact id
   * @param version version
   * @param directory directory
   * @param resolution resolution
   * @param consumer consumer
   * @return jar file
   * @throws IOException if the url constructed cannot be found
   */
  @NotNull
  public static File downloadAndLoadDependency(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String directory,
      @NotNull final DependencyResolution resolution,
      @NotNull final LongConsumer consumer)
      throws IOException {
    final File f = downloadFile(groupId, artifactId, version, directory, resolution, consumer);
    loadDependency(f);
    return f;
  }

  /**
   * Gets Maven Central URL of Maven Dependency.
   *
   * @param dependency the dependency
   * @return the maven central url
   * @deprecated See {@link #getRepoUrl(RepositoryDependency)}
   */
  @LegacyApi(since = "1.3.0")
  @Deprecated
  @NotNull
  public static String getMavenCentralUrl(@NotNull final RepositoryDependency dependency) {
    return getDependencyUrl(dependency);
  }

  /**
   * Gets Jitpack URL of Jitpack Dependency.
   *
   * @param dependency the dependency
   * @return the jitpack url
   * @deprecated See {@link #getRepoUrl(RepositoryDependency)}
   */
  @LegacyApi(since = "1.3.0")
  @Deprecated
  @NotNull
  public static String getJitpackUrl(@NotNull final RepositoryDependency dependency) {
    return getDependencyUrl(dependency);
  }

  /**
   * Gets Dependency Repository URL of Maven/Jitpack Dependency.
   *
   * @param dependency the dependency
   * @return the jitpack url
   */
  @NotNull
  public static String getRepoUrl(@NotNull final RepositoryDependency dependency) {
    return getDependencyUrl(dependency);
  }

  /**
   * Constructs dependency URL of MavenDependency.
   *
   * @param dependency the dependency
   * @return the dependency url
   */
  @NotNull
  public static String getDependencyUrl(@NotNull final RepositoryDependency dependency) {
    return String.format(
        "%s%s/%s/%s/",
        dependency.getResolution().getBaseUrl(),
        dependency.getGroup().replaceAll("\\.", "/"),
        dependency.getArtifact(),
        dependency.getVersion());
  }

  /**
   * Constructs dependency URL directly based on parameters.
   *
   * @param groupId the group id
   * @param artifactId the artifact id
   * @param version the version
   * @param base the base
   * @return the dependency url
   */
  @NotNull
  public static String getDependencyUrl(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String base) {
    return String.format("%s%s/%s/%s/", base, groupId.replaceAll("\\.", "/"), artifactId, version);
  }

  /**
   * Download dependency file.
   *
   * @param dependency the dependency
   * @param link the link
   * @param parent the parent
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadFile(
      @NotNull final RepositoryDependency dependency,
      @NotNull final String link,
      @NotNull final String parent)
      throws IOException {
    final String file = dependency.getArtifact() + "-" + dependency.getVersion() + ".jar";
    final String url = link + file;
    return downloadFile(Paths.get(parent + "/" + file), url);
  }

  /**
   * Download dependency file with consumer.
   *
   * @param dependency the dependency
   * @param link the link
   * @param parent the parent
   * @param consumer the consumer
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadFile(
      @NotNull final RepositoryDependency dependency,
      @NotNull final String link,
      @NotNull final String parent,
      @NotNull final LongConsumer consumer)
      throws IOException {
    final String file = dependency.getArtifact() + "-" + dependency.getVersion() + ".jar";
    final String url = link + file;
    return downloadFile(Paths.get(parent + "/" + file), url, consumer);
  }

  /**
   * Download dependency file.
   *
   * @param groupId the group id
   * @param artifactId the artifact id
   * @param version the version
   * @param parent the parent
   * @param resolution the resolution
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadFile(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String parent,
      @NotNull final DependencyResolution resolution)
      throws IOException {
    final String file = artifactId + "-" + version + ".jar";
    final String url =
        getDependencyUrl(groupId, artifactId, version, resolution.getBaseUrl()) + file;
    return downloadFile(Paths.get(parent + "/" + file), url);
  }

  /**
   * Download dependency file with consumer.
   *
   * @param groupId the group id
   * @param artifactId the artifact id
   * @param version the version
   * @param parent the parent
   * @param consumer the consumer
   * @param resolution the resolution
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadFile(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String parent,
      @NotNull final DependencyResolution resolution,
      @NotNull final LongConsumer consumer)
      throws IOException {
    final String file = artifactId + "-" + version + ".jar";
    final String url =
        getDependencyUrl(groupId, artifactId, version, resolution.getBaseUrl()) + file;
    return downloadFile(Paths.get(parent + "/" + file), url, consumer);
  }

  /**
   * Download dependency file.
   *
   * @param p the p
   * @param url the url
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadFile(@NotNull final Path p, @NotNull final String url)
      throws IOException {
    Logger.info("Downloading Dependency at " + url + " into folder " + p);
    final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
    final FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(p));
    final byte[] dataBuffer = new byte[131072];
    int bytesRead;
    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
      fileOutputStream.write(dataBuffer, 0, bytesRead);
    }
    return new File(p.toString());
  }

  /**
   * Download dependency file with hook included.
   *
   * @param p the p
   * @param url the url
   * @param progress the consumer
   * @return the file
   * @throws IOException the io exception
   */
  @NotNull
  public static File downloadFile(
      @NotNull final Path p, @NotNull final String url, @NotNull final LongConsumer progress)
      throws IOException {
    Logger.info("Downloading Dependency at " + url + " into folder " + p);
    final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
    final FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(p));
    final byte[] dataBuffer = new byte[131072];
    int bytesRead;
    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
      progress.accept(bytesRead);
      fileOutputStream.write(dataBuffer, 0, bytesRead);
    }
    return new File(p.toString());
  }

  /**
   * Gets file size from link.
   *
   * @param url the link
   * @return long size
   * @throws IOException if url is invalid
   */
  public static long getFileSize(@NotNull final String url) throws IOException {
    final URL download = new URL(url);
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) download.openConnection();
      conn.setRequestMethod("HEAD");
      return conn.getContentLengthLong();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Load JAR file.
   *
   * @param file the file
   * @throws IOException the io exception
   */
  public static void loadDependency(@NotNull final File file) throws IOException {
    Logger.info("Loading JAR Dependency at: " + file.getAbsolutePath());
    try {
      ADD_URL_METHOD.invoke(CLASSLOADER, file.toURI().toURL());
    } catch (final IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    Logger.info("Finished Loading Dependency " + file.getName());
  }

  /**
   * Checks if VLC installation exists or not.
   *
   * @param library the library
   * @return whether vlc can be found or not
   */
  public static boolean checkVLCExistance(@NotNull final MinecraftMediaLibrary library) {
    String keyword = "libvlc";
    if (RuntimeUtilities.isWindows()) {
      keyword += ".dll";
    } else if (RuntimeUtilities.isMac()) {
      keyword += ".dylib";
    } else if (RuntimeUtilities.isLinux()) {
      keyword += ".so";
    }
    final Queue<File> folders = new ArrayDeque<>();
    folders.add(new File(library.getPlugin().getDataFolder(), "vlc"));
    while (!folders.isEmpty()) {
      final File f = folders.remove();
      if (f.isDirectory()) {
        folders.addAll(Arrays.asList(f.listFiles()));
      } else {
        if (f.getName().equals(keyword)) {
          Logger.info("Found VLC Installation on this Server! Good Job :)");
          NATIVE_VLC_PATH = f.getParentFile();
          NativeLibrary.addSearchPath(
              RuntimeUtil.getLibVlcLibraryName(), NATIVE_VLC_PATH.getAbsolutePath());
          new NativeDiscovery().discover();
          return true;
        }
      }
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

  /**
   * Sets the classloader used for dependency loading.
   *
   * @param CLASSLOADER the classloader
   */
  public static void setClassloader(final URLClassLoader CLASSLOADER) {
    DependencyUtilities.CLASSLOADER = CLASSLOADER;
  }
}
