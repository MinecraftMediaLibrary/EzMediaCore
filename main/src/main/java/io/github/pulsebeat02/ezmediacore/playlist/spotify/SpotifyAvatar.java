/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import org.jetbrains.annotations.NotNull;

public final class SpotifyAvatar implements Avatar {

  private final se.michaelthelin.spotify.model_objects.specification.Image image;
  private final Dimension dimension;

  SpotifyAvatar(@NotNull final se.michaelthelin.spotify.model_objects.specification.Image image) {
    this.image = image;
    this.dimension = Dimension.ofDimension(image.getWidth(), image.getHeight());
  }

  @Override
  public @NotNull String getUrl() {
    return this.image.getUrl();
  }

  @NotNull
  se.michaelthelin.spotify.model_objects.specification.Image getImage() {
    return this.image;
  }

  @Override
  public @NotNull Dimension getDimensions() {
    return this.dimension;
  }
}
