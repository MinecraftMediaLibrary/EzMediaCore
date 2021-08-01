package io.github.pulsebeat02.ezmediacore.http.request;

import org.jetbrains.annotations.NotNull;

public interface ZipRequest {

  @NotNull
  default ZipHeader getHeader() {
    return ZipHeader.ZIP;
  }

  void setZipHeader(@NotNull final ZipHeader header);
}
