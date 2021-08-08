package io.github.pulsebeat02.ezmediacore.utility;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public final class FileUtils {

  private FileUtils() {
  }

  @NotNull
  public static Path downloadImageFile(@NotNull final String url, @NotNull final Path path)
      throws IOException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be null or empty!");
    final String filePath = "%s/%s.png".formatted(path, UUID.randomUUID());
    try (final InputStream in = new URL(url).openStream()) {
      Files.copy(in, Path.of(filePath));
    }
    return Path.of(filePath);
  }

  public static void createFile(@NotNull final Path file) throws IOException {
    final Path parent = file.getParent();
    if (Files.notExists(parent)) {
      Files.createDirectories(file.getParent());
    }
    if (Files.notExists(file)) {
      Files.createFile(file);
    }
  }

  public static boolean createIfNotExists(@NotNull final Path file) throws IOException {
    if (Files.notExists(file)) {
      Files.createFile(file);
      return true;
    }
    return false;
  }

  public static boolean createFolderIfNotExists(@NotNull final Path file) throws IOException {
    if (Files.notExists(file)) {
      Files.createDirectory(file);
      return true;
    }
    return false;
  }

  public static void copyURLToFile(@NotNull final String url, @NotNull final Path path) {
    try (final ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
        final FileChannel channel = new FileOutputStream(path.toString()).getChannel()) {
      channel.transferFrom(in, 0, Long.MAX_VALUE);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static String getFirstLine(@NotNull final Path file) throws IOException {
    return Files.lines(file).findFirst().orElseThrow(AssertionError::new);
  }
}
