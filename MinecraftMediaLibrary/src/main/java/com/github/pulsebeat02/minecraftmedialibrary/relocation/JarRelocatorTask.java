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

package com.github.pulsebeat02.minecraftmedialibrary.relocation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A task that copies {@link JarEntry jar entries} from a {@link JarFile jar input} to a {@link
 * JarOutputStream jar output}*, applying the relocations defined by a {@link RelocatingRemapper}.
 */
final class JarRelocatorTask {
  private final RelocatingRemapper remapper;
  private final JarOutputStream jarOut;
  private final JarFile jarIn;

  private final Set<String> resources = new HashSet<>();

  /**
   * Instantiates a new Jar relocator task.
   *
   * @param remapper the remapper
   * @param jarOut the jar out
   * @param jarIn the jar in
   */
  JarRelocatorTask(
      final RelocatingRemapper remapper, final JarOutputStream jarOut, final JarFile jarIn) {
    this.remapper = remapper;
    this.jarOut = jarOut;
    this.jarIn = jarIn;
  }

  private static void copy(final InputStream from, final OutputStream to) throws IOException {
    final byte[] buf = new byte[8192];
    while (true) {
      final int n = from.read(buf);
      if (n == -1) {
        break;
      }
      to.write(buf, 0, n);
    }
  }

  /**
   * Process entries.
   *
   * @throws IOException the io exception
   */
  void processEntries() throws IOException {
    for (final Enumeration<JarEntry> entries = jarIn.entries(); entries.hasMoreElements(); ) {
      final JarEntry entry = entries.nextElement();

      // The 'INDEX.LIST' file is an optional file, containing information about the packages
      // defined in a jar. Instead of relocating the entries in it, we delete it, since it is
      // optional anyway.
      //
      // We don't process directory entries, and instead opt to recreate them when adding
      // classes/resources.
      if (entry.getName().equals("META-INF/INDEX.LIST") || entry.isDirectory()) {
        continue;
      }

      try (final InputStream entryIn = jarIn.getInputStream(entry)) {
        processEntry(entry, entryIn);
      }
    }
  }

  private void processEntry(final JarEntry entry, final InputStream entryIn) throws IOException {
    final String name = entry.getName();
    final String mappedName = remapper.map(name);

    // ensure the parent directory structure exists for the entry.
    processDirectory(mappedName, true);

    if (name.endsWith(".class")) {
      processClass(name, entryIn);
    } else if (!resources.contains(mappedName)) {
      processResource(mappedName, entryIn, entry.getTime());
    }
  }

  private void processDirectory(final String name, final boolean parentsOnly) throws IOException {
    final int index = name.lastIndexOf('/');
    if (index != -1) {
      final String parentDirectory = name.substring(0, index);
      if (!resources.contains(parentDirectory)) {
        processDirectory(parentDirectory, false);
      }
    }

    if (parentsOnly) {
      return;
    }

    // directory entries must end in "/"
    final JarEntry entry = new JarEntry(name + "/");
    jarOut.putNextEntry(entry);
    resources.add(name);
  }

  private void processResource(
      final String name, final InputStream entryIn, final long lastModified) throws IOException {
    final JarEntry jarEntry = new JarEntry(name);
    jarEntry.setTime(lastModified);

    jarOut.putNextEntry(jarEntry);
    copy(entryIn, jarOut);

    resources.add(name);
  }

  private void processClass(final String name, final InputStream entryIn) throws IOException {
    final ClassReader classReader = new ClassReader(entryIn);
    final ClassWriter classWriter = new ClassWriter(0);
    final RelocatingClassVisitor classVisitor =
        new RelocatingClassVisitor(classWriter, remapper, name);

    try {
      classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
    } catch (final Throwable e) {
      throw new RuntimeException("Error processing class " + name, e);
    }

    final byte[] renamedClass = classWriter.toByteArray();

    // Need to take the .class off for remapping evaluation
    final String mappedName = remapper.map(name.substring(0, name.indexOf('.')));

    // Now we put it back on so the class file is written out with the right extension.
    jarOut.putNextEntry(new JarEntry(mappedName + ".class"));
    jarOut.write(renamedClass);
  }
}
