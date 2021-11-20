package io.github.pulsebeat02.ezmediacore.junit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import org.junit.jupiter.api.Test;

public final class YoutubeScratching {

  @Test
  public void getPopularVideo() {
    assertTrue(MediaExtractionUtils.getFirstResultVideo("despacito").isPresent());
  }

}
