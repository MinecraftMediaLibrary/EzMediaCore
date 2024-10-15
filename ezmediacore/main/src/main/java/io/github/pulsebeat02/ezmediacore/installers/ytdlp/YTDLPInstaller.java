package io.github.pulsebeat02.ezmediacore.installers.ytdlp;

import com.google.common.collect.Table;
import io.github.pulsebeat02.ezmediacore.installers.BaseInstaller;
import io.github.pulsebeat02.ezmediacore.util.io.ResourceUtils;
import io.github.pulsebeat02.ezmediacore.util.os.OS;

import java.nio.file.Path;

/** YT-DLP installer class. */
public final class YTDLPInstaller extends BaseInstaller {

  private static final Table<OS, Boolean, String> BITS_64;
  private static final Table<OS, Boolean, String> BITS_32;

  static {
    BITS_64 = ResourceUtils.parseTable("/installers/ytdlp/bits64.json");
    BITS_32 = ResourceUtils.parseTable("/installers/ytdlp/bits32.json");
  }

  YTDLPInstaller(final Path folder) {
    super(folder, "ffmpeg", BITS_32, BITS_64);
  }

  YTDLPInstaller() {
    super("ffmpeg", BITS_32, BITS_64);
  }

  /**
   * Constructs a new YTDLPInstaller with the specified directory for the executable.
   *
   * @param executable directory
   * @return new FFmpegInstaller
   */
  public static YTDLPInstaller create(final Path executable) {
    return new YTDLPInstaller(executable);
  }

  /**
   * Constructs a new YTDLPInstaller with the default directory for the executable.
   *
   * <p>For Windows, it is C:/Program Files/static-emc Otherwise, it is [user home
   * directory]/static-emc
   *
   * @return new YTDLPInstaller
   */
  public static YTDLPInstaller create() {
    return new YTDLPInstaller();
  }

  @Override
  public boolean isSupported() {
    return true;
  }
}
