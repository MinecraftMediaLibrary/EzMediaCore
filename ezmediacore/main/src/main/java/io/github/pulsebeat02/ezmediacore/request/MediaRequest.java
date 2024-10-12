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
package io.github.pulsebeat02.ezmediacore.request;

import io.github.pulsebeat02.ezmediacore.jlibdl.component.Format;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.MediaInfo;
import io.github.pulsebeat02.ezmediacore.player.input.Input;

import java.util.List;
import org.jetbrains.annotations.Contract;


public final class MediaRequest {

  private final MediaInfo request;
  private final List<Format> audio;
  private final List<Format> video;
  private final boolean stream;

  MediaRequest( final MediaInfo request) {
    this.request = request;
    this.audio = this.getInternalAudioInputs();
    this.video = this.getInternalVideoInputs();
    this.stream = this.isStream();
  }

  MediaRequest() {
    this.request = null;
    this.audio = List.of();
    this.video = List.of();
    this.stream = false;
  }

  @Contract("_ -> new")
  public static  MediaRequest ofRequest( final MediaInfo request) {
    return new MediaRequest(request);
  }

  @Contract(" -> new")
  public static  MediaRequest ofEmptyRequest() {
    return new MediaRequest();
  }

  private  List<Format> getInternalAudioInputs() {
    final List<Format> formats = this.request.getFormats();
    return formats.stream().filter(this::isAudioCodec).toList();
  }

  private boolean isAudioCodec( final Format format) {
    final String acodec = format.getAcodec();
    return !acodec.equals("none");
  }

  private  List<Format> getInternalVideoInputs() {
    final List<Format> formats = this.request.getFormats();
    return formats.stream().filter(this::isVideoCodec).toList();
  }

  private boolean isVideoCodec( final Format format) {
    final String vcodec = format.getVcodec();
    return !vcodec.equals("none");
  }

  private boolean isStreamInternal() {
    return this.request.isLive();
  }

  public MediaInfo getRequest() {
    return this.request;
  }

  public List<Format> getAudioLinks() {
    return this.audio;
  }

  public List<Format> getVideoLinks() {
    return this.video;
  }

  public boolean isStream() {
    return this.stream;
  }
}
