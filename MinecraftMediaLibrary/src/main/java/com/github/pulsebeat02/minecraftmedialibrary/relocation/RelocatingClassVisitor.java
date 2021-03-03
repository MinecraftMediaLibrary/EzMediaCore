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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

/** A {@link ClassVisitor} that relocates types and names with a {@link RelocatingRemapper}. */
final class RelocatingClassVisitor extends ClassRemapper {
  private final String packageName;

  /**
   * Instantiates a new Relocating class visitor.
   *
   * @param writer the writer
   * @param remapper the remapper
   * @param name the name
   */
  RelocatingClassVisitor(
      final ClassWriter writer, final RelocatingRemapper remapper, final String name) {
    super(writer, remapper);
    packageName = name.substring(0, name.lastIndexOf('/') + 1);
  }

  @Override
  public void visitSource(final String source, final String debug) {
    if (source == null) {
      super.visitSource(null, debug);
      return;
    }

    // visit source file name
    final String name = packageName + source;
    final String mappedName = super.remapper.map(name);
    final String mappedFileName = mappedName.substring(mappedName.lastIndexOf('/') + 1);
    super.visitSource(mappedFileName, debug);
  }
}
