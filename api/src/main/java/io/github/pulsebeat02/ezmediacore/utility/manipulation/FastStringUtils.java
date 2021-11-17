/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.utility.manipulation;

import io.github.pulsebeat02.ezmediacore.utility.search.BNDM;
import io.github.pulsebeat02.ezmediacore.utility.search.StringSearch;
import org.jetbrains.annotations.NotNull;

public final class FastStringUtils {

  private static final StringSearch STRING_SEARCHER;

  static {
    STRING_SEARCHER = new BNDM();
  }

  private FastStringUtils() {
  }

  public static int fastQuerySearch(@NotNull final String content, @NotNull final String target) {
    return STRING_SEARCHER.searchString(content, target);
  }

  public static int fastQuerySearch(
      @NotNull final String content, @NotNull final String target, final int after) {
    return STRING_SEARCHER.searchString(content, after, target);
  }

  public static int fastQuerySearch(
      @NotNull final String content, @NotNull final String target, final int start, final int end) {
    return STRING_SEARCHER.searchString(content, start, end, target);
  }

  public static int fastQuerySearch(final byte[] content, final byte[] target) {
    return STRING_SEARCHER.searchBytes(content, target);
  }

  public static int fastQuerySearch(final byte[] content, final byte[] target, final int after) {
    return STRING_SEARCHER.searchBytes(content, after, target);
  }

  public static int fastQuerySearch(
      final byte[] content, final byte[] target, final int start, final int end) {
    return STRING_SEARCHER.searchBytes(content, start, end, target);
  }

  public static int fastQuerySearch(final char[] content, final char[] target) {
    return STRING_SEARCHER.searchChars(content, target);
  }

  public static int fastQuerySearch(final char[] content, final char[] target, final int after) {
    return STRING_SEARCHER.searchChars(content, after, target);
  }

  public static int fastQuerySearch(
      final char[] content, final char[] target, final int start, final int end) {
    return STRING_SEARCHER.searchChars(content, start, end, target);
  }
}
