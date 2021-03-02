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

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.CPUArchitecture;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LinuxPackageJSONTest {

  public static void main(final String[] args) {
    try {
      final TypeToken<ArrayListMultimap<String, LinuxPackage>> type = new TypeToken<ArrayListMultimap<String, LinuxPackage>>() { };
      final Gson gson =
          new GsonBuilder()
              .registerTypeHierarchyAdapter(
                  type.getRawType(), new ArrayListMultimapLinuxAdapter())
              .setPrettyPrinting()
              .create();
      gson.fromJson(getFileContents(), type.getType());
    } catch (final IOException e) {
      e.printStackTrace();
    }
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

  private static final class ArrayListMultimapLinuxAdapter
      extends TypeAdapter<ArrayListMultimap<String, LinuxPackage>> {

    @Override
    public void write(@NotNull final JsonWriter out, @NotNull final ArrayListMultimap<String, LinuxPackage> map)
        throws IOException {
      out.beginObject();
      for (final String key : map.keySet()) {
        final List<LinuxPackage> values = map.get(key);
        out.name(key);
        out.beginArray();
        for (final LinuxPackage entry : values) {
          out.beginObject();
          out.value(entry.getArch().toString());
          out.value(entry.getUrl());
          out.value(entry.getMirror());
          out.endObject();
        }
        out.endArray();
      }
      out.endObject();
    }

    @Override
    public ArrayListMultimap<String, LinuxPackage> read(@NotNull final JsonReader in) throws IOException {
      in.beginObject();
      final ArrayListMultimap<String, LinuxPackage> map = ArrayListMultimap.create();
      while (in.hasNext()) {
        final String key = in.nextName();
        in.beginArray();
        while (in.hasNext()) {
          in.beginObject();
          final String archName = in.nextString();
          final CPUArchitecture arch = CPUArchitecture.fromName(archName);
          if (arch == null) {
            throw new IllegalArgumentException("Argument: " + archName + " is not a valid argument!");
          }
          final String url = in.nextString();
          final String mirror = in.nextString();
          in.endObject();
          map.put(key, new LinuxPackage(url, mirror, arch));
        }
        in.endArray();
      }
      in.endObject();
      return map;
    }
  }
}
