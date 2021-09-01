package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.io.IOException;

public class YoutubeRequestTest {

  public static void main(final String[] args) throws IOException {
    final long time = System.currentTimeMillis();
    System.out.println(MediaExtractionUtils.getFirstResultVideo("despacito").get());
    System.out.println("Time: " + (System.currentTimeMillis() - time));
  }
}
