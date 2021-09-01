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
/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherSetting;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public final class VideoCommandAttributes {

  private final AtomicBoolean completion;

  private VideoPlayer player;
  private DitherSetting dither;
  private VideoType mode;
  private String video;
  private Path audio;

  private boolean youtube;
  private String mrl; // for file resource load video mrl
  private int map;

  private int frameWidth;
  private int frameHeight;
  private int pixelWidth;
  private int pixelHeight;

  private String url; // for resourcepack url
  private byte[] hash;

  public VideoCommandAttributes() {
    this.dither = DitherSetting.FILTER_LITE;
    this.frameWidth = 5;
    this.frameHeight = 5;
    this.pixelWidth = 640;
    this.pixelHeight = 360;
    this.completion = new AtomicBoolean(false);
    this.mode = VideoType.ITEMFRAME;
  }

  public @NotNull DitherSetting getDither() {
    return this.dither;
  }

  public void setDither(final DitherSetting dither) {
    this.dither = dither;
  }

  public @NotNull VideoPlayer getPlayer() {
    return this.player;
  }

  public void setPlayer(@NotNull final VideoPlayer player) {
    this.player = player;
  }

  public @NotNull String getVideoMrl() {
    return this.video;
  }

  public void setVideoMrl(@NotNull final String video) {
    this.video = video;
  }

  public boolean isYoutube() {
    return this.youtube;
  }

  public void setYoutube(final boolean youtube) {
    this.youtube = youtube;
  }

  public int getFrameWidth() {
    return this.frameWidth;
  }

  public void setFrameWidth(final int frameWidth) {
    this.frameWidth = frameWidth;
  }

  public int getFrameHeight() {
    return this.frameHeight;
  }

  public void setFrameHeight(final int frameHeight) {
    this.frameHeight = frameHeight;
  }

  public int getPixelWidth() {
    return this.pixelWidth;
  }

  public void setPixelWidth(final int pixelWidth) {
    this.pixelWidth = pixelWidth;
  }

  public int getPixelHeight() {
    return this.pixelHeight;
  }

  public void setPixelHeight(final int pixelHeight) {
    this.pixelHeight = pixelHeight;
  }

  public int getMap() {
    return this.map;
  }

  public void setMap(final int map) {
    this.map = map;
  }

  public @NotNull AtomicBoolean getCompletion() {
    return this.completion;
  }

  public @NotNull VideoType getVideoType() {
    return this.mode;
  }

  public void setVideoType(@NotNull final VideoType type) {
    this.mode = type;
  }

  public @NotNull VideoType getMode() {
    return this.mode;
  }

  public void setMode(@NotNull final VideoType mode) {
    this.mode = mode;
  }

  public @NotNull String getUrl() {
    return this.url;
  }

  public void setUrl(@NotNull final String url) {
    this.url = url;
  }

  public byte @NotNull [] getHash() {
    return this.hash;
  }

  public void setHash(final byte @NotNull [] hash) {
    this.hash = hash;
  }

  public @NotNull Path getAudio() {
    return this.audio;
  }

  public void setAudio(@NotNull final Path audio) {
    this.audio = audio;
  }

  public @NotNull String getMrl() {
    return this.mrl;
  }

  public void setMrl(@NotNull final String mrl) {
    this.mrl = mrl;
  }
}
