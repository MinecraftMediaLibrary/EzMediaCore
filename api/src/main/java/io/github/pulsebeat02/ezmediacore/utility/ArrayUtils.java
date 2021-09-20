package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ArrayUtils {

  private ArrayUtils() {}

  public static <T> T @NotNull [] trim(
      final T @NotNull [] array, final int startInclusive, final int endExclusive) {
    return Arrays.copyOfRange(array, startInclusive, endExclusive);
  }

  public static <T> void parallel(final T @NotNull [] array, @NotNull final Consumer<T> consumer) {
    stream(array).parallel().forEach(consumer);
  }

  public static <T> void parallelSneaky(
      final T @NotNull [] array, @NotNull final Consumer<T> consumer) {
    stream(array).parallel().forEach(ThrowingConsumer.sneaky(consumer));
  }

  public static <T> void parallelUnchecked(
      final T @NotNull [] array, @NotNull final Consumer<T> consumer) {
    stream(array).parallel().forEach(ThrowingConsumer.unchecked(consumer));
  }

  public static <T> @NotNull List<T> list(final T @NotNull [] array) {
    return stream(array).toList();
  }

  public static <T> @NotNull Set<T> set(final T @NotNull [] array) {
    return stream(array).collect(Collectors.toUnmodifiableSet());
  }

  public static <T> @NotNull Set<T> mutableSet(final T @NotNull [] array) {
    return stream(array).collect(Collectors.toSet());
  }

  public static <T> @NotNull List<T> mutableList(final T @NotNull [] array) {
    return stream(array).collect(Collectors.toList());
  }

  @Contract(pure = true)
  private static <T> @NotNull Stream<T> stream(final T @NotNull [] array) {
    return Arrays.stream(array);
  }
}
