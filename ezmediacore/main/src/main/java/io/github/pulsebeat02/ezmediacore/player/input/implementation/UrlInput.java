/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.player.input.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import org.jetbrains.annotations.Contract;


public final class UrlInput implements Input {

  private static final Input EMPTY_URL;

  static {
    EMPTY_URL = ofUrl("https://example.com/");
  }

  private final URL url;

  UrlInput( final String url) {
    checkNotNull(url, "URL specified cannot be null!");
    try {
      this.url = new URL(url);
      this.url.openConnection();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Invalid url %s!".formatted(url));
    }
  }

  @Contract(value = "_ -> new", pure = true)
  public static  Input ofUrl( final String url) {
    return new UrlInput(url);
  }

  public static  Input emptyUrl() {
    return EMPTY_URL;
  }

  @Override
  public  String getInput() {
    return this.url.toString();
  }

  @Override
  public void setupInput() {}

  @Contract(pure = true)
  @Override
  public  String toString() {
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
