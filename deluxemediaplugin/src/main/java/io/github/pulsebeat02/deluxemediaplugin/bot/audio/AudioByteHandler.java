package io.github.pulsebeat02.deluxemediaplugin.bot.audio;

import java.nio.ByteBuffer;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

public class AudioByteHandler implements AudioSendHandler {



  @Override
  public boolean canProvide() {
    return false;
  }

  @Nullable
  @Override
  public ByteBuffer provide20MsAudio() {
    return null;
  }
}
