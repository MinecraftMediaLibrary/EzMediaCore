package io.github.pulsebeat02.epicmedialib.http.request;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface FileRequest extends Request {

  @NotNull
  Path requestFileCallback(@NotNull final String request);

  @NotNull
  ZipHeader getHeader();
}
