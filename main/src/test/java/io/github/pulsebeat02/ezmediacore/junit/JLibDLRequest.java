package io.github.pulsebeat02.ezmediacore.junit;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import org.junit.jupiter.api.Test;

public final class JLibDLRequest {

  @Test
  public void createRequest() {
    final Input mrl = UrlInput.ofUrl("https://youtu.be/DHYcRU50yM0");
    final MediaRequest request = RequestUtils.requestMediaInformation(mrl);
    System.out.println(request.isStream());
    System.out.println(request.getAudioLinks());
    System.out.println(request.getVideoLinks());
  }
}
