/*
 * Copyright Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pulsebeat02.minecraftmedialibrary.relocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** A relocation rule */
public final class Relocation {
  private final String pattern;
  private final String relocatedPattern;
  private final String pathPattern;
  private final String relocatedPathPattern;

  private final Set<String> includes;
  private final Set<String> excludes;

  /**
   * Creates a new relocation
   *
   * @param pattern the pattern to match
   * @param relocatedPattern the pattern to relocate to
   * @param includes a collection of patterns which this rule should specifically include
   * @param excludes a collection of patterns which this rule should specifically exclude
   */
  public Relocation(
      final String pattern,
      final String relocatedPattern,
      final Collection<String> includes,
      final Collection<String> excludes) {
    this.pattern = pattern.replace('/', '.');
    this.pathPattern = pattern.replace('.', '/');
    this.relocatedPattern = relocatedPattern.replace('/', '.');
    this.relocatedPathPattern = relocatedPattern.replace('.', '/');

    if (includes != null && !includes.isEmpty()) {
      this.includes = normalizePatterns(includes);
      this.includes.addAll(includes);
    } else {
      this.includes = null;
    }

    if (excludes != null && !excludes.isEmpty()) {
      this.excludes = normalizePatterns(excludes);
      this.excludes.addAll(excludes);
    } else {
      this.excludes = null;
    }
  }

  /**
   * Creates a new relocation with no specific includes or excludes
   *
   * @param pattern the pattern to match
   * @param relocatedPattern the pattern to relocate to
   */
  public Relocation(final String pattern, final String relocatedPattern) {
    this(pattern, relocatedPattern, Collections.emptyList(), Collections.emptyList());
  }

  private static Set<String> normalizePatterns(final Collection<String> patterns) {
    final Set<String> normalized = new LinkedHashSet<>();
    for (final String pattern : patterns) {
      final String classPattern = pattern.replace('.', '/');
      normalized.add(classPattern);
      if (classPattern.endsWith("/*")) {
        final String packagePattern = classPattern.substring(0, classPattern.lastIndexOf('/'));
        normalized.add(packagePattern);
      }
    }
    return normalized;
  }

  private boolean isIncluded(final String path) {
    if (this.includes == null) {
      return true;
    }

    for (final String include : this.includes) {
      if (SelectorUtils.matchPath(include, path, true)) {
        return true;
      }
    }
    return false;
  }

  private boolean isExcluded(final String path) {
    if (this.excludes == null) {
      return false;
    }

    for (final String exclude : this.excludes) {
      if (SelectorUtils.matchPath(exclude, path, true)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Can relocate path boolean.
   *
   * @param path the path
   * @return the boolean
   */
  boolean canRelocatePath(String path) {
    if (path.endsWith(".class")) {
      path = path.substring(0, path.length() - 6);
    }

    if (!isIncluded(path) || isExcluded(path)) {
      return false;
    }

    return path.startsWith(this.pathPattern) || path.startsWith("/" + this.pathPattern);
  }

  /**
   * Can relocate class boolean.
   *
   * @param clazz the clazz
   * @return the boolean
   */
  boolean canRelocateClass(final String clazz) {
    return clazz.indexOf('/') == -1 && canRelocatePath(clazz.replace('.', '/'));
  }

  /**
   * Relocate path string.
   *
   * @param path the path
   * @return the string
   */
  String relocatePath(final String path) {
    return path.replaceFirst(this.pathPattern, this.relocatedPathPattern);
  }

  /**
   * Relocate class string.
   *
   * @param clazz the clazz
   * @return the string
   */
  String relocateClass(final String clazz) {
    return clazz.replaceFirst(this.pattern, this.relocatedPattern);
  }
}
