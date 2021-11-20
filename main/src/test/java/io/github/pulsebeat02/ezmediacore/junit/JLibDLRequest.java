package io.github.pulsebeat02.ezmediacore.junit;

import static org.junit.Assert.assertFalse;

import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import org.junit.Test;

public final class JLibDLRequest {

  @Test
  public void createRequest() {
    final MrlConfiguration mrl = MrlConfiguration.ofMrl("https://youtu.be/DHYcRU50yM0");
    assertFalse(RequestUtils.isStream(mrl));
    assertFalse(RequestUtils.getVideoURLs(mrl).isEmpty());
    assertFalse(RequestUtils.getAudioURLs(mrl).isEmpty());
  }

}
