package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import java.io.IOException;
import java.nio.file.Path;

public class FileDownloadHeaderTest {

  public static void main(final String[] args) throws IOException, InterruptedException {
    DependencyUtils.downloadFile(
        Path.of(System.getProperty("user.dir"), "test", "vlc.dmg"),
        "https://get.videolan.org/vlc/3.0.16/macosx/vlc-3.0.16-arm64.dmg");
  }
}
