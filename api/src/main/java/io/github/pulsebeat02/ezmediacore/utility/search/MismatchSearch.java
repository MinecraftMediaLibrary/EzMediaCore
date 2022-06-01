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
package io.github.pulsebeat02.ezmediacore.utility.search;

/*
 * MismatchSearch.java
 *
 * Created on 12.11.2003.
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

/**
 * Subclasses of MismatchSearch allow for searching with a fixed number of possible errors.
 * Subclasses of this class return an <code>int</code> array of two elements with the first <code>
 * int</code> being the position at which the hit occurred and the second <code>int</code> being the
 * number of mismatches at the position.
 *
 * <p>Example:
 *
 * <pre>
 * int[] positions = new ShiftOrMismatches().searchString("this is null",
 * "nu1l", 1);
 * </pre>
 *
 * <p>positions[0] would be 8, positions[1] (the number of mismatches) would be 1.
 *
 * @author <a href="http://johannburkard.de">Johann Burkard</a>
 * @version $Id: MismatchSearch.java 6675 2015-01-17 21:02:35Z johann $
 * @see <a href="http://johannburkard.de/software/stringsearch/">StringSearch &#8211;
 *     high-performance pattern matching algorithms in Java</a>
 */
public abstract class MismatchSearch extends StringSearch {

  /*
   * Pre-processing methods
   */

  /**
   * Pre-processes the pattern, allowing <b>zero</b> errors.
   *
   * <p>Identical to <code>process(pattern, 0)</code>
   *
   * @param pattern the <code>byte</code> array containing the pattern, may not be <code>null</code>
   * @see #processBytes(byte[], int)
   */
  @Override
  public final Object processBytes(final byte[] pattern) {
    return this.processBytes(pattern, 0);
  }

  /**
   * Pre-processes the pattern, allowing k errors.
   *
   * @param pattern the <code>byte</code> array containing the pattern, may not be <code>null</code>
   * @param k the editing distance
   * @return an Object
   */
  public abstract Object processBytes(byte[] pattern, int k);

  /**
   * Pre-processes the pattern, allowing <b>zero</b> errors.
   *
   * <p>Identical to <code>process(pattern, 0)</code>.
   *
   * @param pattern a <code>char</code> array containing the pattern, may not be <code>null</code>
   * @return an Object
   * @see #processChars(char[], int)
   */
  @Override
  public final Object processChars(final char[] pattern) {
    return this.processChars(pattern, 0);
  }

  /**
   * Pre-processes a <code>char</code> array, allowing k errors.
   *
   * @param pattern a <code>char</code> array containing the pattern, may not be <code>null</code>
   * @param k the editing distance
   * @return an Object
   */
  public abstract Object processChars(char[] pattern, int k);

  /**
   * Pre-processes a String, allowing k errors. This method should not be used directly because it
   * is implicitly called in the {@link #searchString(String, String)} methods.
   *
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param k the editing distance
   * @return an Object
   */
  public Object processString(final String pattern, final int k) {
    return this.processChars(StringSearch.getChars(pattern), k);
  }

  /*
   * Byte searching methods
   */

  /**
   * @see #searchBytes(byte[], int, int, byte[], Object, int)
   */
  @Override
  public final int searchBytes(
      final byte[] text,
      final int textStart,
      final int textEnd,
      final byte[] pattern,
      final Object processed) {

    return this.searchBytes(text, textStart, textEnd, pattern, processed, 0)[0];
  }

  /**
   * Returns the position in the text at which the pattern was found. Returns -1 if the pattern was
   * not found.
   *
   * @param text the <code>byte</code> array containing the text, may not be <code>null</code>
   * @param pattern the <code>byte</code> array containing the pattern, may not be <code>null</code>
   * @param k the editing distance
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchBytes(byte[], int, int, byte[], Object, int)
   */
  public final int[] searchBytes(final byte[] text, final byte[] pattern, final int k) {
    return this.searchBytes(text, 0, text.length, pattern, this.processBytes(pattern, k), k);
  }

  /**
   * Returns the position in the text at which the pattern was found. Returns -1 if the pattern was
   * not found.
   *
   * @param text the <code>byte</code> array containing the text, may not be <code>null</code>
   * @param pattern the <code>byte</code> array containing the pattern, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processBytes(byte[], int)}, may not be
   *     <code>null</code>
   * @param k the editing distance
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchBytes(byte[], int, int, byte[], Object, int)
   */
  public final int[] searchBytes(
      final byte[] text, final byte[] pattern, final Object processed, final int k) {

    return this.searchBytes(text, 0, text.length, pattern, processed, k);
  }

  /**
   * Returns the position in the text at which the pattern was found. Returns -1 if the pattern was
   * not found.
   *
   * @param text the <code>byte</code> array containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param pattern the <code>byte</code> array containing the pattern, may not be <code>null</code>
   * @param k the editing distance
   * @return int the position in the text or -1 if the pattern was not found
   * @see #searchBytes(byte[], int, int, byte[], Object, int)
   */
  public final int[] searchBytes(
      final byte[] text, final int textStart, final byte[] pattern, final int k) {

    return this.searchBytes(
        text, textStart, text.length, pattern, this.processBytes(pattern, k), k);
  }

  /**
   * Returns the position in the text at which the pattern was found. Returns -1 if the pattern was
   * not found.
   *
   * @param text the <code>byte</code> array containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param k the editing distance
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchBytes(byte[], int, int, byte[], Object, int)
   */
  public final int[] searchBytes(
      final byte[] text,
      final int textStart,
      final byte[] pattern,
      final Object processed,
      final int k) {

    return this.searchBytes(text, textStart, text.length, pattern, processed, k);
  }

  /**
   * Returns the position in the text at which the pattern was found. Returns -1 if the pattern was
   * not found.
   *
   * @param text text the <code>byte</code> array containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param textEnd at which position in the text comparing should stop
   * @param pattern the <code>byte</code> array containing the pattern, may not be <code>null</code>
   * @param k the editing distance
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchBytes(byte[], int, int, byte[], Object, int)
   */
  public final int[] searchBytes(
      final byte[] text,
      final int textStart,
      final int textEnd,
      final byte[] pattern,
      final int k) {

    return this.searchBytes(text, textStart, textEnd, pattern, this.processBytes(pattern, k), k);
  }

  /**
   * Returns the position in the text at which the pattern was found. Returns -1 if the pattern was
   * not found.
   *
   * @param text text the <code>byte</code> array containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param textEnd at which position in the text comparing should stop
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processBytes(byte[], int)}, may not be
   *     <code>null</code>
   * @param k the editing distance
   * @return the position in the text or -1 if the pattern was not found
   * @see #processBytes(byte[], int)
   */
  public abstract int[] searchBytes(
      byte[] text, int textStart, int textEnd, byte[] pattern, Object processed, int k);

  /*
   * Char searching methods
   */

  /**
   * Finder for the given pattern in the text, starting at textStart and comparing to at most
   * textEnd, allowing zero errors.
   *
   * @see StringSearch#searchChars(char[], int, int, char[], Object)
   * @see #processChars(char[], int)
   */
  @Override
  public final int searchChars(
      final char[] text,
      final int textStart,
      final int textEnd,
      final char[] pattern,
      final Object processed) {

    return this.searchChars(text, textStart, textEnd, pattern, processed, 0)[0];
  }

  /**
   * Finder for the given pattern in the text, allowing k errors.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object, int)
   */
  public final int[] searchChars(final char[] text, final char[] pattern, final int k) {
    return this.searchChars(text, 0, text.length, pattern, this.processChars(pattern, k), k);
  }

  /**
   * Finder for the given pattern in the text, allowing k errors.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processChars(char[], int)} or {@link
   *     #processString(String, int)}, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object, int)
   */
  public final int[] searchChars(
      final char[] text, final char[] pattern, final Object processed, final int k) {

    return this.searchChars(text, 0, text.length, pattern, processed, k);
  }

  /**
   * Finder for the given pattern in the text, starting at textStart, allowing k errors.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object)
   */
  public final int[] searchChars(
      final char[] text, final int textStart, final char[] pattern, final int k) {

    return this.searchChars(
        text, textStart, text.length, pattern, this.processChars(pattern, k), k);
  }

  /**
   * Finder for the given pattern in the text, starting at textStart, allowing k errors.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processChars(char[], int)} or {@link
   *     #processString(String, int)}, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object, int)
   */
  public final int[] searchChars(
      final char[] text,
      final int textStart,
      final char[] pattern,
      final Object processed,
      final int k) {

    return this.searchChars(text, textStart, text.length, pattern, processed, k);
  }

  /**
   * Finder for the given pattern in the text, starting at textStart and comparing to at most
   * textEnd, allowing k errors.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param textEnd at which position in the text comparing should stop
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   */
  public final int[] searchChars(
      final char[] text,
      final int textStart,
      final int textEnd,
      final char[] pattern,
      final int k) {

    return this.searchChars(text, textStart, textEnd, pattern, this.processChars(pattern, k), k);
  }

  /**
   * Finder for the given pattern in the text, starting at textStart and comparing to at most
   * textEnd, allowing k errors.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param textEnd at which position in the text comparing should stop
   * @param pattern the pattern to search for, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processChars(char[], int)} or {@link
   *     #processString(String, int)}, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   */
  public abstract int[] searchChars(
      char[] text, int textStart, int textEnd, char[] pattern, Object processed, int k);

  /* String searching methods */

  /**
   * Convenience method to search for patterns in Strings. Returns the position in the text at which
   * the pattern was found. Returns -1 if the pattern was not found.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], int)
   */
  public final int[] searchString(final String text, final String pattern, final int k) {
    return this.searchString(text, 0, text.length(), pattern, k);
  }

  /**
   * Convenience method to search for patterns in Strings. Returns the position in the text at which
   * the pattern was found. Returns -1 if the pattern was not found.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processChars(char[], int)} or {@link
   *     #processString(String, int)}, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object, int)
   */
  public final int[] searchString(
      final String text, final String pattern, final Object processed, final int k) {

    return this.searchString(text, 0, text.length(), pattern, processed, k);
  }

  /**
   * Convenience method to search for patterns in Strings. Returns the position in the text at which
   * the pattern was found. Returns -1 if the pattern was not found.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], int)
   */
  public final int[] searchString(
      final String text, final int textStart, final String pattern, final int k) {

    return this.searchString(text, textStart, text.length(), pattern, k);
  }

  /**
   * Convenience method to search for patterns in Strings. Returns the position in the text at which
   * the pattern was found. Returns -1 if the pattern was not found.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processChars(char[], int)} or {@link
   *     #processString(String, int)}, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object, int)
   */
  public final int[] searchString(
      final String text,
      final int textStart,
      final String pattern,
      final Object processed,
      final int k) {

    return this.searchString(text, textStart, text.length(), pattern, processed, k);
  }

  /**
   * Convenience method to search for patterns in Strings. Returns the position in the text at which
   * the pattern was found. Returns -1 if the pattern was not found.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param textEnd at which position in the text comparing should stop
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], int)
   */
  public final int[] searchString(
      final String text,
      final int textStart,
      final int textEnd,
      final String pattern,
      final int k) {

    return StringSearch.activeStringAccess.searchString(text, textStart, textEnd, pattern, k, this);
  }

  /**
   * Convenience method to search for patterns in Strings. Returns the position in the text at which
   * the pattern was found. Returns -1 if the pattern was not found.
   *
   * @param text the String containing the text, may not be <code>null</code>
   * @param textStart at which position in the text the comparing should start
   * @param textEnd at which position in the text comparing should stop
   * @param pattern the String containing the pattern, may not be <code>null</code>
   * @param processed an Object as returned from {@link #processChars(char[], int)} or {@link
   *     #processString(String, int)}, may not be <code>null</code>
   * @param k the maximum number of mismatches (the editing distance)
   * @return the position in the text or -1 if the pattern was not found
   * @see #searchChars(char[], int, int, char[], Object, int)
   */
  public final int[] searchString(
      final String text,
      final int textStart,
      final int textEnd,
      final String pattern,
      final Object processed,
      final int k) {

    return StringSearch.activeStringAccess.searchString(
        text, textStart, textEnd, pattern, processed, k, this);
  }
}
