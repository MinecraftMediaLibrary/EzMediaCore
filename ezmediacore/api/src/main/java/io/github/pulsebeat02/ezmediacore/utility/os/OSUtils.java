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
package io.github.pulsebeat02.ezmediacore.utility.os;

import com.google.common.collect.Sets;
import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

public final class OSUtils {

  private OSUtils() {}

  public static @NotNull String getLinuxDistribution() {
    try {
      final Set<Path> candidates = createCandidates();
      return readCandidates(candidates);
    } catch (final IOException e) {
      return "";
    }
  }

  private static @NotNull Set<Path> createCandidates() throws IOException {
    final Set<Path> candidates = Sets.newHashSet();
    candidates.addAll(getEtcFiles());
    candidates.addAll(getProcFiles());
    return candidates;
  }

  private static @NotNull String readCandidates(@NotNull final Set<Path> candidates)
      throws IOException {
    final StringBuilder builder = new StringBuilder();
    for (final Path path : candidates) {
      builder.append(readFileContents(path));
    }
    return builder.toString();
  }

  private static @NotNull String readFileContents(@NotNull final Path file) throws IOException {
    final StringBuilder builder = new StringBuilder();
    try (final BufferedReader reader = Files.newBufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
    }
    return builder.toString();
  }

  private static @NotNull @Unmodifiable Set<Path> getProcFiles() {
    final Path proc = Path.of("/proc/version");
    if (Files.exists(proc)) {
      return Set.of(proc);
    }
    return Set.of();
  }

  private static @NotNull @Unmodifiable Set<Path> getEtcFiles() throws IOException {
    final Path etc = Path.of("/etc/");
    final Set<Path> candidates = Sets.newHashSet();
    if (Files.exists(etc)) {
      final CustomFileVisitor visitor = new CustomFileVisitor();
      Files.walkFileTree(etc, visitor);
      candidates.addAll(visitor.getEtcFiles());
    }
    return candidates;
  }

  private static class CustomFileVisitor extends SimpleFileVisitor<Path> {

    private final Set<Path> etc;

    {
      this.etc = Sets.newHashSet();
    }

    @Override
    public FileVisitResult visitFile(
        @NotNull final Path path, @NotNull final BasicFileAttributes attrs) {

      if (!Files.isRegularFile(path)) {
        return FileVisitResult.CONTINUE;
      }

      if (!Files.isReadable(path)) {
        return FileVisitResult.CONTINUE;
      }

      if (!this.isReleaseFile(path)) {
        return FileVisitResult.CONTINUE;
      }

      this.etc.add(path);

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
      return FileVisitResult.CONTINUE;
    }

    public Set<Path> getEtcFiles() {
      return this.etc;
    }

    private boolean isReleaseFile(@NotNull final Path path) {
      return PathUtils.getName(path).endsWith("-release");
    }
  }
}
