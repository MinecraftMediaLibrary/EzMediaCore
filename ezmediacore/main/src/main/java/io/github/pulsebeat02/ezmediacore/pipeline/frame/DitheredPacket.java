package io.github.pulsebeat02.ezmediacore.pipeline.frame;

public interface DitheredPacket extends FramePacket {
  byte[] getMapDitheredSamples();
}
