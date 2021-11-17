package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import java.nio.file.Path;

public class MacTest {

  public static void main(final String[] args) {
    System.out.println(PathUtils.getName(Path.of(System.getProperty("user.dir"), "test")));
  }
}
