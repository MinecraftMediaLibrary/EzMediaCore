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

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxOSPackages;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class LinuxPackageJSONTest {

  private static final TypeToken<Map<String, List<LinuxPackage>>>
      MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN;
  private static final TypeToken<Map<String, LinuxOSPackages>>
      MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN;
  private static final Gson GSON;

  static {
    MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN = new TypeToken<Map<String, List<LinuxPackage>>>() {};
    MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN = new TypeToken<Map<String, LinuxOSPackages>>() {};
    GSON =
        new GsonBuilder()
            .registerTypeAdapter(LinuxOSPackages.class, new LinuxOSPackagesAdapter())
            .setPrettyPrinting()
            .create();
  }

  public static void main(final String[] args) throws IOException {
    final Map<String, List<LinuxPackage>> packages =
        GSON.fromJson(getFileContents(), MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN.getType());
  }

  private static String getFileContents() throws IOException {
    final String name = "linux-package-installation.json";
    final ClassLoader loader = LinuxPackageJSONTest.class.getClassLoader();
    final InputStream input = loader.getResourceAsStream(name);
    if (input == null) {
      throw new IllegalArgumentException("file not found! " + name);
    } else {
      return IOUtils.toString(input, StandardCharsets.UTF_8.name());
    }
  }

  private static final class LinuxOSPackagesAdapter extends TypeAdapter<LinuxOSPackages> {

    @Override
    public void write(final JsonWriter out, final LinuxOSPackages linuxOSPackages) {
      GSON.toJson(
          linuxOSPackages.getLinks().asMap(),
          MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN.getType(),
          out);
    }

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
