package io.github.pulsebeat02.ezmediacore.vlc;

import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class VLCBinaryChecksum implements ChecksumVerification {

  private final String url;
  private final Path path;

  public VLCBinaryChecksum(@NotNull final String url, @NotNull final Path path) {
    this.url = url;
    this.path = path;
  }

  @Override
  public @NotNull Path downloadFile() throws IOException {
    final String hash = getHash(url);
    final Path file = DependencyUtils.downloadFile(path, url);
    if (!HashingUtils.getHash(file).equals(hash)) {
      return DependencyUtils.downloadFile(path, url);
    }
    return file;
  }

  @Override
  public @NotNull String getHash(@NotNull String url) throws IOException {
    return RequestUtils.getResult(
            "%s%s.sha1"
                .formatted(
                    RequestUtils.getParentUrl(url), FilenameUtils.getName(new URL(url).getPath())))
        .substring(0, 40);
  }

  @Override
  public @NotNull Path getPath() {
    return path;
  }

  @Override
  public @NotNull String getUrl() {
    return url;
  }
}
