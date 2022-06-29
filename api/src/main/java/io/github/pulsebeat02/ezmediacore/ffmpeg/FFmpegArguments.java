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

  String VIDEO_CODEC = "-vcodec";
  String VIDEO_RESOLUTION = "-s";
  String VIDEO_FRAME_RATE = "-r";
  String VIDEO_ASPECT_RATIO = "-aspect";
  String VIDEO_LOOP = "-loop";

  String DURATION_START = "-ss";
  String DURATION_END = "-to";

  String NATIVE_FRAME_READ_RATE = "-re";
  String NO_CONSOLE_INPUT = "-nostdin";
  String LOG_LEVEL = "-loglevel";
  String HIDE_BANNER = "-hide_banner";
  String NO_STATS = "-nostats";
  String PIPE_OUTPUT = "pipe:%s";
  String PIPE_TO_STDOUT = "-";
}
