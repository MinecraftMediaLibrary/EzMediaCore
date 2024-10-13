package rewrite.pipeline.input.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class InputMediaDownloader {

  private final DownloadableInput input;
  private final ExecutorService service;

  public InputMediaDownloader(final DownloadableInput input, final ExecutorService service) {
    this.input = input;
    this.service = service;
  }

  public InputMediaDownloader(final DownloadableInput input) {
    this(input, Executors.newSingleThreadExecutor());
  }

  public CompletableFuture<Path> download(final Path target) {
    try (this.service) {
      final CompletableFuture<String> future = this.input.getMediaRepresentation();
      return future.thenApplyAsync(media -> this.downloadFile(media, target), this.service);
    }
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
