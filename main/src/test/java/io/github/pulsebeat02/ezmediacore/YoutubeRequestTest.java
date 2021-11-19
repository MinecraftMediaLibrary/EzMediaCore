package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import java.io.IOException;

public class YoutubeRequestTest {

  public static void main(final String[] args) {
    final long time = System.currentTimeMillis();
    //noinspection OptionalGetWithoutIsPresent
    System.out.println(MediaExtractionUtils.getFirstResultVideo("despacito").get());
    System.out.println("Time: " + (System.currentTimeMillis() - time));
  }
}
