package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.vlc.os.unix.dependency.UnixPackageInstaller;
import java.nio.file.Path;

public class UnixPackageTest {

  public static void main(final String[] args) {
    new UnixPackageInstaller(Path.of(System.getProperty("user.dir")).resolve("test")).start();
  }

}
