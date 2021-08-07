package io.github.pulsebeat02.ezmediacore.format;

import java.text.SimpleDateFormat;

public final class FormatterProvider {

  public static final SimpleDateFormat FFMPEG_TIME_FORMATTER;

  static {
    FFMPEG_TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");
  }

  private FormatterProvider() {
  }
}
