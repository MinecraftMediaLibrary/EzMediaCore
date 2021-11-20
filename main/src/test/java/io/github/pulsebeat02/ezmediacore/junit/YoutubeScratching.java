package io.github.pulsebeat02.ezmediacore.junit;

import static org.junit.Assert.assertTrue;

import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import org.junit.Test;

public final class YoutubeScratching {

  @Test
  public void getPopularVideo() {
    assertTrue(MediaExtractionUtils.getFirstResultVideo("despacito").isPresent());
  }

}
