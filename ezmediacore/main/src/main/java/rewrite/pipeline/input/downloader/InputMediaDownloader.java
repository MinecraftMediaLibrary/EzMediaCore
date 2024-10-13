package rewrite.pipeline.input.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public final class InputMediaDownloader {

  private final DownloadableInput input;

  public InputMediaDownloader(final DownloadableInput input) {
    this.input = input;
  }

  public CompletableFuture<Path> download(final Path target) {
    final CompletableFuture<String> future = this.input.getMediaRepresentation();
    return future.thenApply(media -> this.downloadFile(media, target));
  }

  public Path downloadFile(final String raw, final Path target) {
    try {
      final URI uri = URI.create(raw);
      final URL url = uri.toURL();
      try (final InputStream in = url.openStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return target;
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }
}
