package rewrite.capabilities;

import rewrite.installers.ffmpeg.FFmpegInstaller;

import java.io.IOException;
import java.nio.file.Path;

public final class FFmpegCapability extends Capability {

  private static Path FFMPEG_BINARY_PATH;

  public FFmpegCapability() {
    super(() -> {
      try {
        final FFmpegInstaller installer = FFmpegInstaller.create();
        FFMPEG_BINARY_PATH = installer.download(true);
        return true;
      } catch (final IOException e) {
        return false;
      }
    });
  }

  public Path getBinaryPath() {
    if (this.isDisabled()) {
      throw new IllegalStateException("FFmpeg capability is not enabled!");
    }
    return FFMPEG_BINARY_PATH;
  }
}
