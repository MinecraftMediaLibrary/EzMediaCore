package io.github.pulsebeat02.epicmedialib.ffmpeg;

public interface AudioTrimmer extends FFmpegArgumentPreparation, IOProvider {

  long getStartTime();
}
