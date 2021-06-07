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

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.frame.VLCVideoPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public final class VideoCommandAttributes {

  private final AtomicBoolean completion;
  private final DeluxeMediaPlugin plugin;

  private YoutubeExtraction extractor;
  private VLCVideoPlayer player;
  private DitherHolder dither;
  private VideoType mode;
  private Path file;

  private boolean youtube;
  private int frameWidth;
  private int frameHeight;
  private int screenWidth;
  private int screenHeight;
  private int startingMap;

  private String resourcepackUrl;
  private byte[] hash;

  public VideoCommandAttributes(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    dither = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    frameWidth = 5;
    frameHeight = 5;
    screenWidth = 640;
    screenHeight = 360;
    completion = new AtomicBoolean(false);
    mode = VideoType.ITEMFRAME;
  }

  public DitherHolder getDither() {
    return dither;
  }

  public void setDither(final DitherHolder dither) {
    this.dither = dither;
  }

  public YoutubeExtraction getExtractor() {
    return extractor;
  }

  public void setExtractor(final YoutubeExtraction extractor) {
    this.extractor = extractor;
  }

  public VLCVideoPlayer getPlayer() {
    return player;
  }

  public void setPlayer(final VLCVideoPlayer player) {
    this.player = player;
  }

  public Path getFile() {
    return file;
  }

  public void setFile(final Path file) {
    this.file = file;
  }

  public boolean isYoutube() {
    return youtube;
  }

  public void setYoutube(final boolean youtube) {
    this.youtube = youtube;
  }

  public int getFrameWidth() {
    return frameWidth;
  }

  public void setFrameWidth(final int frameWidth) {
    this.frameWidth = frameWidth;
  }

  public int getFrameHeight() {
    return frameHeight;
  }

  public void setFrameHeight(final int frameHeight) {
    this.frameHeight = frameHeight;
  }

  public int getScreenWidth() {
    return screenWidth;
  }

  public void setScreenWidth(final int screenWidth) {
    this.screenWidth = screenWidth;
  }

  public int getScreenHeight() {
    return screenHeight;
  }

  public void setScreenHeight(final int screenHeight) {
    this.screenHeight = screenHeight;
  }

  public int getStartingMap() {
    return startingMap;
  }

  public void setStartingMap(final int startingMap) {
    this.startingMap = startingMap;
  }

  public AtomicBoolean getCompletion() {
    return completion;
  }

  public VideoType getVideoType() {
    return mode;
  }

  public void setVideoType(@NotNull final VideoType type) {
    mode = type;
  }

  public DeluxeMediaPlugin getPlugin() {
    return plugin;
  }

  public VideoType getMode() {
    return mode;
  }

  public void setMode(final VideoType mode) {
    this.mode = mode;
  }

  public String getResourcepackUrl() {
    return resourcepackUrl;
  }

  public void setResourcepackUrl(final String resourcepackUrl) {
    this.resourcepackUrl = resourcepackUrl;
  }

  public byte[] getHash() {
    return hash;
  }

  public void setHash(final byte[] hash) {
    this.hash = hash;
  }
}
