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
package io.github.pulsebeat02.ezmediacore.request;

import io.github.pulsebeat02.ezmediacore.jlibdl.component.Format;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.MediaInfo;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MediaRequest {

  private final MediaInfo request;
  private final List<Input> audio;
  private final List<Input> video;
  private final boolean stream;

  MediaRequest(@NotNull final MediaInfo request) {
    this.request = request;
    this.audio = this.getInternalAudioInputs();
    this.video = this.getInternalVideoInputs();
    this.stream = this.isStream();
  }

  MediaRequest(@NotNull final Input url) {
    this.request = null;
    this.audio = List.of(url);
    this.video = List.of(url);
    this.stream = false;
  }

  @Contract("_ -> new")
  public static @NotNull MediaRequest ofRequest(@NotNull final MediaInfo request) {
    return new MediaRequest(request);
  }

  @Contract("_ -> new")
  public static @NotNull MediaRequest ofRequest(@NotNull final Input url) {
    return new MediaRequest(url);
  }

  private @NotNull List<Input> getInternalAudioInputs() {
    final List<Format> formats = this.request.getFormats();
    final List<Format> audio = formats.stream().filter(this::isAudioCodec).toList();
    return audio.stream().map(url -> UrlInput.ofUrl(url.getUrl())).toList();
  }

  private boolean isAudioCodec(@NotNull final Format format) {
    return !format.getAcodec().equals("none");
  }

  private @NotNull List<Input> getInternalVideoInputs() {
    final List<Format> formats = this.request.getFormats();
    final List<Format> video = formats.stream().filter(this::isVideoCodec).toList();
    return video.stream().map(url -> UrlInput.ofUrl(url.getUrl())).toList();
  }

  private boolean isVideoCodec(@NotNull final Format format) {
    return !format.getVcodec().equals("none");
  }

  private boolean isStreamInternal() {
    return this.request.isLive();
  }

  public MediaInfo getRequest() {
    return this.request;
  }

  public List<Input> getAudioLinks() {
    return this.audio;
  }

  public List<Input> getVideoLinks() {
    return this.video;
  }

  public boolean isStream() {
    return this.stream;
  }
}
