package io.github.pulsebeat02.ezmediacore.player.input.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.player.input.InputItem;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class UrlInput implements InputItem {

  private static final InputItem EMPTY_URL;

  static {
    EMPTY_URL = ofUrl("");
  }

  private final URL url;

  UrlInput(@NotNull final String url) {
    checkNotNull(url, "URL specified cannot be null!");
    try {
      this.url = new URL(url);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid url %s!".formatted(url));
    }
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull InputItem ofUrl(@NotNull final String url) {
    return new UrlInput(url);
  }

  public static @NotNull InputItem emptyUrl() {
    return EMPTY_URL;
  }

  @Override
  public @NotNull String getInput() {
    return this.url.toString();
  }

  @Override
  public void setupInput() {
  }

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{url=%s}".formatted(this.url);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof UrlInput)) {
      return false;
    }
    return ((UrlInput) obj).url.equals(this.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.url);
  }
}
