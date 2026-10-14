/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.media.player.multimedia.vlc;

import java.awt.Dimension;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.VideoApi;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;

// Tells VLC which buffer to render into when the format of a video is known. VLC reports the size it decodes at, which
// can include padding, for example 1920x1088 for a 1080p video. The visible size comes from the video track instead,
// and the VideoRenderer chooses the buffer: the decoded size, or the size attached to the player, which makes VLC scale
// the picture itself.
final class FrameFormatCallback extends BufferFormatCallbackAdapter {

  private final VideoRenderer renderer;
  private final MediaPlayer player;

  FrameFormatCallback(final VideoRenderer renderer, final MediaPlayer player) {
    this.renderer = renderer;
    this.player = player;
  }

  // Chooses an RV32 buffer for the size VLC decodes at. The visible size is the size of the video track, limited to the
  // decoded size, or the decoded size if the track does not report a usable size.
  @Override
  public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
    final VideoApi videoApi = this.player.video();
    final Dimension trackSize = videoApi.videoDimension();
    int visibleWidth = sourceWidth;
    int visibleHeight = sourceHeight;
    if (trackSize != null && Math.min(trackSize.width, trackSize.height) > 0) {
      visibleWidth = Math.min(sourceWidth, trackSize.width);
      visibleHeight = Math.min(sourceHeight, trackSize.height);
    }

    return this.renderer.createBufferFormat(sourceWidth, sourceHeight, visibleWidth, visibleHeight);
  }
}
