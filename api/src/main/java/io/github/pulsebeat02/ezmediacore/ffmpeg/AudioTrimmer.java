package io.github.pulsebeat02.ezmediacore.ffmpeg;

public interface AudioTrimmer extends FFmpegArgumentPreparation, IOProvider {

  long getStartTime();
}
