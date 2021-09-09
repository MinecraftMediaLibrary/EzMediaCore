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

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public final class AudioCommandAttributes {

  private final AtomicBoolean completion;
  private final String key;

  private Path audio;
  private String link;
  private byte[] hash;

  public AudioCommandAttributes(@NotNull final DeluxeMediaPlugin plugin) {
    this.completion = new AtomicBoolean(true);
    this.key = plugin.getName().toLowerCase();
  }

  public @NotNull AtomicBoolean getCompletion() {
    return this.completion;
  }

  public void setCompletion(final boolean mode) {
    this.completion.set(mode);
  }

  public @NotNull String getKey() {
    return this.key;
  }

  public @NotNull Path getAudio() {
    return this.audio;
  }

  public void setAudio(@NotNull final Path audio) {
    this.audio = audio;
  }

  public @NotNull String getLink() {
    return this.link;
  }

  public void setLink(@NotNull final String link) {
    this.link = link;
  }

  public byte @NotNull [] getHash() {
    return this.hash;
  }

  public void setHash(final byte @NotNull [] hash) {
    this.hash = hash;
  }
}
