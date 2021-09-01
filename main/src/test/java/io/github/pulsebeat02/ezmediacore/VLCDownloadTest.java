package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import java.io.IOException;
import java.nio.file.Path;

public class VLCDownloadTest {

  public static void main(String[] args) throws IOException {
    DependencyUtils.downloadVLCFile(
        Path.of(System.getProperty("user.dir"), "test", "test.dmg"),
        "https://get.videolan.org/vlc/3.0.16/macosx/vlc-3.0.16-arm64.dmg");
  }
}
