/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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
