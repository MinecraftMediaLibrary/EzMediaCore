package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.search.BNDM;
import io.github.pulsebeat02.ezmediacore.search.StringSearch;
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

  public static int fastQuerySearch(@NotNull final String content, @NotNull final String target,
      final int after) {
    return STRING_SEARCHER.searchString(content, after, target);
  }

  public static int fastQuerySearch(@NotNull final String content, @NotNull final String target,
      final int start, final int end) {
    return STRING_SEARCHER.searchString(content, start, end, target);
  }

  public static int fastQuerySearch(final byte[] content, final byte[] target) {
    return STRING_SEARCHER.searchBytes(content, target);
  }

  public static int fastQuerySearch(final byte[] content, final byte[] target, final int after) {
    return STRING_SEARCHER.searchBytes(content, after, target);
  }

  public static int fastQuerySearch(final byte[] content, final byte[] target, final int start,
      final int end) {
    return STRING_SEARCHER.searchBytes(content, start, end, target);
  }

  public static int fastQuerySearch(final char[] content, final char[] target) {
    return STRING_SEARCHER.searchChars(content, target);
  }

  public static int fastQuerySearch(final char[] content, final char[] target, final int after) {
    return STRING_SEARCHER.searchChars(content, after, target);
  }

  public static int fastQuerySearch(final char[] content, final char[] target, final int start,
      final int end) {
    return STRING_SEARCHER.searchChars(content, start, end, target);
  }
}
