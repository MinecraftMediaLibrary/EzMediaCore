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

package io.github.pulsebeat02.minecraftmedialibrary.relocation;

import org.objectweb.asm.commons.Remapper;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Remaps class names and types using defined {@link Relocation} rules. */
final class RelocatingRemapper extends Remapper {
  private static final Pattern CLASS_PATTERN = Pattern.compile("(\\[*)?L(.+);");

  private final Collection<Relocation> rules;

  /**
   * Instantiates a new Relocating remapper.
   *
   * @param rules the rules
   */
  RelocatingRemapper(final Collection<Relocation> rules) {
    this.rules = rules;
  }

  @Override
  public Object mapValue(final Object object) {
    if (object instanceof String) {
      final String relocatedName = relocate((String) object, true);
      if (relocatedName != null) {
        return relocatedName;
      }
    }
    return super.mapValue(object);
  }

  @Override
  public String map(final String name) {
    final String relocatedName = relocate(name, false);
    if (relocatedName != null) {
      return relocatedName;
    }
    return super.map(name);
  }

  private String relocate(String name, final boolean isClass) {
    String prefix = "";
    String suffix = "";

    final Matcher m = CLASS_PATTERN.matcher(name);
    if (m.matches()) {
      prefix = String.format("%sL", m.group(1));
      suffix = ";";
      name = m.group(2);
    }

    for (final Relocation r : rules) {
      if (isClass && r.canRelocateClass(name)) {
        return prefix + r.relocateClass(name) + suffix;
      } else if (r.canRelocatePath(name)) {
        return prefix + r.relocatePath(name) + suffix;
      }
    }

    return null;
  }
}
