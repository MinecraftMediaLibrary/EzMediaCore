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

// Counts the frames the video renderer of a session shows and the frames it drops for being late, and reports a stretch
// of video in which it dropped most of them. A source that decodes, or a pipeline that runs, slower than the video
// plays freezes the picture without a word otherwise, which looks like a hang. A stretch is ten seconds of video by its
// own timestamps, so a pause, which hands over no frames, does not count. It is reported when at least ten of its
// frames and more than half of them were dropped, and at most once a minute of video. A still picture keeps its frames
// on time and is never reported. Instances belong to the video renderer of one session and are not thread-safe.
final class LateFrameCounter {

  private static final long WINDOW_MICROSECONDS = 10_000_000L;

  // How many frames of a stretch must have been dropped at least to report it.
  private static final int MINIMUM_DROPPED = 10;

  private static final long REPORT_INTERVAL_MICROSECONDS = 60_000_000L;

  private final PlaybackSession.LagListener listener;

  private boolean counting;
  private long windowStartMicroseconds;
  private int shown;
  private int dropped;
  private boolean reported;
  private long lastReportMicroseconds;

  LateFrameCounter(final PlaybackSession.LagListener listener) {
    this.listener = listener;
  }

  void count(final long timestampMicroseconds, final boolean late) {
    if (!this.counting || timestampMicroseconds < this.windowStartMicroseconds) {
      this.counting = true;
      this.reported = false;
      this.begin(timestampMicroseconds);
    }

    if (late) {
      this.dropped++;
    } else {
      this.shown++;
    }

    if (timestampMicroseconds - this.windowStartMicroseconds < WINDOW_MICROSECONDS) {
      return;
    }

    final boolean behind = this.dropped >= MINIMUM_DROPPED && this.dropped > this.shown;
    final boolean quiet = !this.reported || timestampMicroseconds - this.lastReportMicroseconds >= REPORT_INTERVAL_MICROSECONDS;
    if (behind && quiet) {
      this.reported = true;
      this.lastReportMicroseconds = timestampMicroseconds;
      final int frames = this.shown + this.dropped;
      this.listener.fellBehind(this.dropped, frames);
    }

    this.begin(timestampMicroseconds);
  }

  private void begin(final long timestampMicroseconds) {
    this.windowStartMicroseconds = timestampMicroseconds;
    this.shown = 0;
    this.dropped = 0;
  }
}
