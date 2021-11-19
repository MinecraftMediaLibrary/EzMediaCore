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
package io.github.pulsebeat02.ezmediacore.playlist;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.mutable.MutableInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class WebsiteControls implements WebPlayerControls {

  public WebsiteControls(final String url, @NotNull final PlaylistType type) {
    final Collection<String> songs = this.getSongs();
    final MutableInt index = new MutableInt(0);
  }

  @Contract(pure = true)
  private @NotNull @Unmodifiable List<String> getSongs() {
    // TODO: 7/30/2021 get songs from spotify using some api
    return Collections.emptyList();
  }

  @Override
  public void skipSong() {
  }

  @Override
  public void previousSong() {
  }

  @Override
  public void pauseSong() {
  }

  @Override
  public void resumeSong() {
  }

  @Override
  public void seekToTime(final int seconds) {
  }

  @Override
  public void randomize() {
  }

  @Override
  public void loopMode(final boolean mode) {
  }
}
