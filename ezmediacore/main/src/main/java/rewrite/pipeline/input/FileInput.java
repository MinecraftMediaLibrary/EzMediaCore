package rewrite.pipeline.input;

import java.nio.file.Path;

public final class FileInput implements Input {

  private final Path path;

  public FileInput(final Path path) {
    this.path = path;
  }

  @Override
  public String getMediaRepresentation() {
    final Path absolute = this.path.toAbsolutePath();
    final String raw = absolute.toString();
    return raw;
  }
}
