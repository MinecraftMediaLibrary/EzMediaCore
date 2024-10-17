package io.github.pulsebeat02.ezmediacore.pipeline.frame;

public interface FramePacket {

  int[] getRGBSamples();

  byte[] getAudioSamples();

  int getImageWidth();

  int getImageHeight();

  Object getMetadata();
}
