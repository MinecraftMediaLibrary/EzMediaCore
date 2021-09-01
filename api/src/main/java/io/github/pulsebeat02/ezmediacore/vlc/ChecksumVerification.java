package io.github.pulsebeat02.ezmediacore.vlc;

import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface ChecksumVerification {

  @NotNull
  Path downloadFile() throws IOException;

  @NotNull
  String getHash(@NotNull String url) throws IOException;

  @NotNull
  Path getPath();

  @NotNull
  String getUrl();
}
