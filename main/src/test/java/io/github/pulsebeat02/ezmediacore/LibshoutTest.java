package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.libshout.Jlibshout;
import java.io.File;

public class LibshoutTest {

  public static void main(final String[] args) throws Exception {
    final Jlibshout jlibshout = new Jlibshout("localhost", 8030, "/jshout");
    jlibshout.pushMp3(new File("C:\\bestsongever.mp3"));
  }
}
