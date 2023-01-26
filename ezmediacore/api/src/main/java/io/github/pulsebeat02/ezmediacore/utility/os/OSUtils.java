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
