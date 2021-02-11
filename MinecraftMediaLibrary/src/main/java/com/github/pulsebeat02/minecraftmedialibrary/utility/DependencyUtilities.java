/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.MavenDependency;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DependencyUtilities {

  private static final String MAVEN_CENTRAL_URL;
  private static final String JITPACK_CENTRAL_URL;
  private static URLClassLoader CLASSLOADER;
  private static Method ADD_URL_METHOD;

  static {
    Logger.info("Attempting to Open Reflection Module...");
    try {
      Class<?> moduleClass = Class.forName("java.lang.Module");
      Method getModuleMethod = Class.class.getMethod("getModule");
      Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);
      Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
      Object thisModule = getModuleMethod.invoke(DependencyUtilities.class);
      addOpensMethod.invoke(
          urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
      Logger.info(
          "User is using Java 8+, meaning Reflection Module does have to be opened. You may safely ignore this error.");
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException ignored) {
      Logger.info(
          "User is using Java 8, meaning Reflection Module does NOT have to be opened. You may safely ignore this error.");
      // Java 8 doesn't have module class -- you can ignore the error.
    }
    MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";
    JITPACK_CENTRAL_URL = "https://jitpack.io/";
    try {
      CLASSLOADER = (URLClassLoader) ClassLoader.getSystemClassLoader();
      ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      ADD_URL_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public static File downloadMavenDependency(
      @NotNull final MavenDependency dependency, @NotNull final String parent) throws IOException {
    return downloadFile(dependency, getMavenCentralUrl(dependency), parent);
  }

  public static File downloadJitpackDependency(
      @NotNull final MavenDependency dependency, @NotNull final String parent) throws IOException {
    return downloadFile(dependency, getJitpackUrl(dependency), parent);
  }

  public static String getMavenCentralUrl(@NotNull final MavenDependency dependency) {
    return getDependencyUrl(dependency, MAVEN_CENTRAL_URL);
  }

  public static String getJitpackUrl(@NotNull final MavenDependency dependency) {
    return getDependencyUrl(dependency, JITPACK_CENTRAL_URL);
  }

  public static String getDependencyUrl(
      @NotNull final MavenDependency dependency, @NotNull final String base) {
    return base
        + dependency.getGroup().replaceAll("\\.", "/")
        + "/"
        + dependency.getArtifact()
        + "/"
        + dependency.getVersion()
        + "/";
  }

  public static String getDependencyUrl(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String base) {
    return base + groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/";
  }

  public static File downloadFile(
      @NotNull final MavenDependency dependency,
      @NotNull final String link,
      @NotNull final String parent)
      throws IOException {
    final String file = dependency.getArtifact() + "-" + dependency.getVersion() + ".jar";
    final String url = link + file;
    return downloadFile(Paths.get(parent + "/" + file), url);
  }

  public static File downloadFile(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String parent)
      throws IOException {
    final String file = artifactId + "-" + version + ".jar";
    final String url = getDependencyUrl(groupId, artifactId, version, MAVEN_CENTRAL_URL) + file;
    return downloadFile(Paths.get(parent + "/" + file), url);
  }

  public static File downloadFile(@NotNull final Path p, @NotNull final String url)
      throws IOException {
    Logger.info("Downloading Dependency at " + url + " into folder " + p);
    final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
    final FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(p));
    final byte[] dataBuffer = new byte[256000];
    int bytesRead;
    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
      fileOutputStream.write(dataBuffer, 0, bytesRead);
    }
    return new File(p.toString());
  }

  public static void loadDependency(@NotNull final File file) throws IOException {
    Logger.info("Loading JAR Dependency at: " + file.getAbsolutePath());
    try {
      ADD_URL_METHOD.invoke(CLASSLOADER, file.toURI().toURL());
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    Logger.info("Finished Loading Dependency " + file.getName());
  }
}
