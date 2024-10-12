package rewrite.capabilities;

import rewrite.installers.FFmpegInstaller;

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
    return FFMPEG_BINARY_PATH;
  }
}
