package io.github.pulsebeat02.epicmedialib.http.request;

import org.jetbrains.annotations.NotNull;

public enum ZipHeader {
  ZIP("application/zip"),
  OCTET_STREAM("application/octet-stream");

  private final String header;

  ZipHeader(@NotNull final String header) {
    this.header = header;
  }

  public String getHeader() {
    return header;
  }
}
