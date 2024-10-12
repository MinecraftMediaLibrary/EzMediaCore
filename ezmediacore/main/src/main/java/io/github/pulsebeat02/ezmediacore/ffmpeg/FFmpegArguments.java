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
package io.github.pulsebeat02.ezmediacore.ffmpeg;

/** Basic FFmpeg Arguments (does not include all as there are too many!) */
public interface FFmpegArguments {

  String INPUT = "-i";
  String OVERWRITE_FILE = "-y";
  String EXCLUDE_VIDEO_STREAMS = "-vn";
  String OUTPUT_FORMAT = "-f";
  String FILTER = "-filter:";

  String AUDIO_CODEC = "-acodec";
  String AUDIO_BITRATE = "-ab";
  String AUDIO_CHANNELS = "-ac";
  String AUDIO_SAMPLING = "-ar";
  String AUDIO_VOLUME = "-vol";
  String AUDIO_QUALITY_LEVEL = "-qscale";
  String AUDIO_BLOCK_SIZE = "-blocksize";

  String VIDEO_CODEC = "-vcodec";
  String VIDEO_RESOLUTION = "-s";
  String VIDEO_FRAME_RATE = "-r";
  String VIDEO_ASPECT_RATIO = "-aspect";
  String VIDEO_LOOP = "-loop";
  String VIDEO_SCALE = "scale=%s:%s";

  String DURATION_START = "-ss";
  String DURATION_END = "-to";

  String NATIVE_FRAME_READ_RATE = "-re";
  String NO_CONSOLE_INPUT = "-nostdin";
  String LOG_LEVEL = "-loglevel";
  String HIDE_BANNER = "-hide_banner";
  String NO_STATS = "-nostats";
  String PIPE_OUTPUT = "pipe:%s";
  String PIPE_TO_STDOUT = "-";
  String TUNE = "-tune";
}
