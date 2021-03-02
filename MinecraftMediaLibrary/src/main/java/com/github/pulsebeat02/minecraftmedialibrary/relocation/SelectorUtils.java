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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This is a stripped down version of org.codehaus.plexus.util.SelectorUtils for use in {@link
 * Relocation}.
 *
 * @author Arnout J. Kuiper <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 * @author Magesh Umasankar
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 */
final class SelectorUtils {
  private static final String PATTERN_HANDLER_PREFIX = "[";
  private static final String PATTERN_HANDLER_SUFFIX = "]";
  private static final String REGEX_HANDLER_PREFIX = "%regex" + PATTERN_HANDLER_PREFIX;
  private static final String ANT_HANDLER_PREFIX = "%ant" + PATTERN_HANDLER_PREFIX;

  /** Private Constructor */
  private SelectorUtils() {}

  private static boolean isAntPrefixedPattern(final String pattern) {
    return pattern.length() > (ANT_HANDLER_PREFIX.length() + PATTERN_HANDLER_SUFFIX.length() + 1)
        && pattern.startsWith(ANT_HANDLER_PREFIX)
        && pattern.endsWith(PATTERN_HANDLER_SUFFIX);
  }

  // When str starts with a File.separator, pattern has to start with a File.separator.
  // When pattern starts with a File.separator, str has to start with a File.separator.
  private static boolean separatorPatternStartSlashMismatch(
      final String pattern, final String str, final String separator) {
    return str.startsWith(separator) != pattern.startsWith(separator);
  }

  /**
   * Match path boolean.
   *
   * @param pattern the pattern
   * @param str the str
   * @param isCaseSensitive the is case sensitive
   * @return the boolean
   */
  public static boolean matchPath(
      final String pattern, final String str, final boolean isCaseSensitive) {
    return matchPath(pattern, str, File.separator, isCaseSensitive);
  }

  private static boolean matchPath(
      String pattern, final String str, final String separator, final boolean isCaseSensitive) {
    if (isRegexPrefixedPattern(pattern)) {
      pattern =
          pattern.substring(
              REGEX_HANDLER_PREFIX.length(), pattern.length() - PATTERN_HANDLER_SUFFIX.length());
      return str.matches(pattern);
    } else {
      if (isAntPrefixedPattern(pattern)) {
        pattern =
            pattern.substring(
                ANT_HANDLER_PREFIX.length(), pattern.length() - PATTERN_HANDLER_SUFFIX.length());
      }
      return matchAntPathPattern(pattern, str, separator, isCaseSensitive);
    }
  }

  private static boolean isRegexPrefixedPattern(final String pattern) {
    return pattern.length() > (REGEX_HANDLER_PREFIX.length() + PATTERN_HANDLER_SUFFIX.length() + 1)
        && pattern.startsWith(REGEX_HANDLER_PREFIX)
        && pattern.endsWith(PATTERN_HANDLER_SUFFIX);
  }

  private static boolean matchAntPathPattern(
      final String pattern,
      final String str,
      final String separator,
      final boolean isCaseSensitive) {
    if (separatorPatternStartSlashMismatch(pattern, str, separator)) {
      return false;
    }
    final String[] patDirs = tokenizePathToString(pattern, separator);
    final String[] strDirs = tokenizePathToString(str, separator);
    return matchAntPathPattern(patDirs, strDirs, isCaseSensitive);
  }

  private static boolean matchAntPathPattern(
      final String[] patDirs, final String[] strDirs, final boolean isCaseSensitive) {
    int patIdxStart = 0;
    int patIdxEnd = patDirs.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strDirs.length - 1;

    // up to first '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      final String patDir = patDirs[patIdxStart];
      if (patDir.equals("**")) {
        break;
      }
      if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
        return false;
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (!patDirs[i].equals("**")) {
          return false;
        }
      }
      return true;
    } else {
      if (patIdxStart > patIdxEnd) {
        // String not exhausted, but pattern is. Failure.
        return false;
      }
    }

    // up to last '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      final String patDir = patDirs[patIdxEnd];
      if (patDir.equals("**")) {
        break;
      }
      if (!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
        return false;
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (!patDirs[i].equals("**")) {
          return false;
        }
      }
      return true;
    }

    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patDirs[i].equals("**")) {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // '**/**' situation, so skip one
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      final int patLength = (patIdxTmp - patIdxStart - 1);
      final int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop:
      for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          final String subPat = patDirs[patIdxStart + j + 1];
          final String subStr = strDirs[strIdxStart + i + j];
          if (!match(subPat, subStr, isCaseSensitive)) {
            continue strLoop;
          }
        }

        foundIdx = strIdxStart + i;
        break;
      }

      if (foundIdx == -1) {
        return false;
      }

      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (!patDirs[i].equals("**")) {
        return false;
      }
    }

    return true;
  }

  private static boolean match(
      final String pattern, final String str, final boolean isCaseSensitive) {
    final char[] patArr = pattern.toCharArray();
    final char[] strArr = str.toCharArray();
    return match(patArr, strArr, isCaseSensitive);
  }

  private static boolean match(
      final char[] patArr, final char[] strArr, final boolean isCaseSensitive) {
    int patIdxStart = 0;
    int patIdxEnd = patArr.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strArr.length - 1;
    char ch;

    boolean containsStar = false;
    for (final char aPatArr : patArr) {
      if (aPatArr == '*') {
        containsStar = true;
        break;
      }
    }

    if (!containsStar) {
      // No '*'s, so we make a shortcut
      if (patIdxEnd != strIdxEnd) {
        return false; // Pattern and string do not have the same size
      }
      for (int i = 0; i <= patIdxEnd; i++) {
        ch = patArr[i];
        if (ch != '?' && !equals(ch, strArr[i], isCaseSensitive)) {
          return false; // Character mismatch
        }
      }
      return true; // String matches against pattern
    }

    if (patIdxEnd == 0) {
      return true; // Pattern contains only '*', which matches anything
    }

    // Process characters before first star
    while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?' && !equals(ch, strArr[strIdxStart], isCaseSensitive)) {
        return false; // Character mismatch
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }
      return true;
    }

    // Process characters after last star
    while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?' && !equals(ch, strArr[strIdxEnd], isCaseSensitive)) {
        return false; // Character mismatch
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }
      return true;
    }

    // process pattern between stars. padIdxStart and patIdxEnd point
    // always to a '*'.
    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patArr[i] == '*') {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // Two stars next to each other, skip the first one.
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      final int patLength = (patIdxTmp - patIdxStart - 1);
      final int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop:
      for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          ch = patArr[patIdxStart + j + 1];
          if (ch != '?' && !equals(ch, strArr[strIdxStart + i + j], isCaseSensitive)) {
            continue strLoop;
          }
        }

        foundIdx = strIdxStart + i;
        break;
      }

      if (foundIdx == -1) {
        return false;
      }

      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    // All characters in the string are used. Check if only '*'s are left
    // in the pattern. If so, we succeeded. Otherwise failure.
    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (patArr[i] != '*') {
        return false;
      }
    }
    return true;
  }

  /**
   * Test if two characters are equal.
   *
   * @param c1 first char
   * @param c2 second char
   * @param isCaseSensitive sensitive
   * @return result
   */
  private static boolean equals(final char c1, final char c2, final boolean isCaseSensitive) {
    if (c1 == c2) {
      return true;
    }
    if (!isCaseSensitive) {
      // NOTE: Try both upper case and lower case as done by String.equalsIgnoreCase()
      return Character.toUpperCase(c1) == Character.toUpperCase(c2)
              || Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }
    return false;
  }

  private static String[] tokenizePathToString(final String path, final String separator) {
    final List<String> ret = new ArrayList<>();
    final StringTokenizer st = new StringTokenizer(path, separator);
    while (st.hasMoreTokens()) {
      ret.add(st.nextToken());
    }
    return ret.toArray(new String[0]);
  }
}
