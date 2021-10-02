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
package io.github.pulsebeat02.ezmediacore.search;

/*
 * BNDM.java
 *
 * Created on 21.10.2003
 *
 * StringSearch - high-performance pattern matching algorithms in Java
 * Copyright (c) 2003-2015 Johann Burkard (<http://johannburkard.de>)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of the Backwards Non-deterministic DAWG (Directed acyclic word graph) Matching
 * algorithm by Gonzalo Navarro and Mathieu Raffinot. See "A Bit-Parallel Approach to Suffix
 * Automata: Fast Extended String Matching" (appeared in <em>Proceedings of the 9th Annual Symposium
 * on Combinatorial Pattern Matching, 1998</em>).
 *
 * <p>
 *
 * @author <a href="http://johannburkard.de">Johann Burkard</a>
 * @version $Id: BNDM.java 6675 2015-01-17 21:02:35Z johann $
 * @see <a href="http://johannburkard.de/software/stringsearch/" target="_top"> StringSearch &#8211;
 * high-performance pattern matching algorithms in Java</a>
 * @see <a href="http://www.dcc.uchile.cl/~gnavarro/ps/cpm98.ps.gz" target="_top">
 * http://www.dcc.uchile.cl/~gnavarro/ps/cpm98.ps.gz </a>
 * @see <a href="http://www-igm.univ-mlv.fr/~raffinot/ftp/cpm98.ps.gz" target="_top">
 * http://www-igm.univ-mlv.fr/~raffinot/ftp/cpm98.ps.gz </a>
 * @see <a href="http://citeseer.ist.psu.edu/navarro98bitparallel.html" target="_top">
 * http://citeseer.ist.psu.edu/navarro98bitparallel.html </a>
 */
public class BNDM extends StringSearch {

  /**
   * Pre-processing of the pattern. The pattern may not exceed 32 bytes in length. If it does,
   * <b>only it's first 32 bytes</b> are processed which might lead to unexpected results. Returns
   * an <code>int</code> array which is serializable.
   */
  @Override
  public Object processBytes(final byte @NotNull [] pattern) {
    final int end = Math.min(pattern.length, 32);

    final int[] b = new int[256];

    int j = 1;
    for (int i = end - 1; i >= 0; --i, j <<= 1) {
      b[this.index(pattern[i])] |= j;
    }

    return b;
  }

  /**
   * Pre-processing of the pattern. The pattern may not exceed 32 bytes in length. If it does,
   * <b>only it's first 32 bytes</b> are processed which might lead to unexpected results. Returns
   * a {@link CharIntMap} which is serializable.
   */
  @Override
  public Object processChars(final char @NotNull [] pattern) {
    final int end = Math.min(pattern.length, 32);

    final CharIntMap b = this.createCharIntMap(pattern, end, 0);

    int j = 1;
    for (int i = end - 1; i >= 0; --i, j <<= 1) {
      b.set(pattern[i], b.get(pattern[i]) | j);
    }

    return b;
  }

  /**
   *
   */
  @Override
  public int searchBytes(
      final byte[] text,
      final int textStart,
      final int textEnd,
      final byte @NotNull [] pattern,
      final Object processed) {

    final int[] t = (int[]) processed;
    final int l = Math.min(pattern.length, 32);

    int d, j, pos, last;
    pos = textStart;
    while (pos <= textEnd - l) {
      j = l - 1;
      last = l;
      d = -1;
      while (d != 0) {
        d &= t[this.index(text[pos + j])];
        if (d != 0) {
          if (j == 0) {
            return pos;
          }
          last = j;
        }
        --j;
        d <<= 1;
      }
      pos += last;
    }

    return -1;
  }

  @Override
  public int searchChars(
      final char[] text,
      final int textStart,
      final int textEnd,
      final char @NotNull [] pattern,
      final Object processed) {

    final CharIntMap b = (CharIntMap) processed;
    final int l = Math.min(pattern.length, 32);

    int d, j, pos, last;
    pos = textStart;
    while (pos <= textEnd - l) {
      j = l - 1;
      last = l;
      d = -1;
      while (d != 0) {
        d &= b.get(text[pos + j]);
        if (d != 0) {
          if (j == 0) {
            return pos;
          }
          last = j;
        }
        --j;
        d <<= 1;
      }
      pos += last;
    }

    return -1;
  }

  /**
   * Returns the smaller of two <code>char</code>s.
   *
   * @param one the first <code>char</code>
   * @param two the second <code>char</code>
   * @return the smaller <code>char</code>
   */
  final char min(final char one, final char two) {
    return one < two ? one : two;
  }

  /**
   * Returns the larger of two <code>char</code>s.
   *
   * @param one the first <code>char</code>
   * @param two the second <code>char</code>
   * @return the larger <code>char</code>
   */
  final char max(final char one, final char two) {
    return one > two ? one : two;
  }
}
