package io.github.pulsebeat02.ezmediacore.junit;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import org.junit.jupiter.api.Test;

public final class JLibDLRequest {

  @Test
  public void createRequest() {
    final Input mrl = UrlInput.ofUrl("https://youtu.be/DHYcRU50yM0");
    assertFalse(RequestUtils.isStream(mrl));
    assertFalse(RequestUtils.getVideoURLs(mrl).isEmpty());
    assertFalse(RequestUtils.getAudioURLs(mrl).isEmpty());
  }
}
