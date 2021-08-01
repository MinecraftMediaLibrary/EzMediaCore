package io.github.pulsebeat02.epicmedialib.dependency;

import org.jetbrains.annotations.NotNull;

public enum Repositories {
  MAVEN("https://repo1.maven.org/maven2"),
  JITPACK("https://jitpack.io");

  private final String url;

  Repositories(@NotNull final String url) {
    this.url = url;
  }

  public String getUrl() {
    return this.url;
  }
}
