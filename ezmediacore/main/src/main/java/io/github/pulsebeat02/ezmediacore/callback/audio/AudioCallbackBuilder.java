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
package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.video.BlockHighlightCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract sealed class AudioCallbackBuilder
    permits FFmpegDiscordCallback.Builder,
        NullCallback.Builder,
        ServerCallback.Builder,
        VLCDiscordCallback.Builder {

  @Contract(value = " -> new", pure = true)
  public static @NotNull FFmpegDiscordCallback.Builder ffmpegDiscord() {
    return new FFmpegDiscordCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull FFmpegHttpServerCallback.Builder ffmpegHttpServer() {
    return new FFmpegHttpServerCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull NullCallback.Builder empty() {
    return new NullCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull PackCallback.Builder pack() {
    return new PackCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull VLCDiscordCallback.Builder vlcDiscord() {
    return new VLCDiscordCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull VLCHttpServerCallback.Builder vlcHttpServer() {
    return new VLCHttpServerCallback.Builder();
  }

  public abstract @NotNull AudioOutput build(@NotNull final MediaLibraryCore core);
}
