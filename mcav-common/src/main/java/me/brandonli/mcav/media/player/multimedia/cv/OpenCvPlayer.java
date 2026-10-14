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
package me.brandonli.mcav.media.player.multimedia.cv;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * A player that decodes with OpenCV, for files and URLs.
 *
 * <p>OpenCV reads a video file through a backend that has to be compiled into it, and the builds of the bundled
 * JavaCPP presets differ: the Windows and macOS builds have one, the Linux build has none and only captures from
 * cameras. Where OpenCV cannot read files, this player reads them with the FFmpeg reader that JavaCV bundles, so a
 * file plays on every platform instead of a video that never shows a frame. The pipelines, the scaling and the
 * playback clock are the same either way.
 */
public final class OpenCvPlayer extends AbstractVideoPlayerCv {

  /**
   * Constructs a new OpenCV player.
   */
  public OpenCvPlayer() {}

  @Override
  protected FrameGrabber createFrameGrabber(final String resource) {
    Preconditions.checkNotNull(resource, "Resource must not be null!");

    final boolean openCvReadsFiles = OpenCvVideoBackends.canDecodeFiles();
    return createFrameGrabber(resource, openCvReadsFiles);
  }

  // Creates the grabber for a resource, for a build that reads files itself or one that does not. Both cases exist on
  // the platforms the library supports, so both can be tested wherever the tests run.
  @VisibleForTesting
  static FrameGrabber createFrameGrabber(final String resource, final boolean openCvReadsFiles) {
    if (openCvReadsFiles) {
      return new OpenCVFrameGrabber(resource);
    }

    return new FFmpegFrameGrabber(resource);
  }
}
