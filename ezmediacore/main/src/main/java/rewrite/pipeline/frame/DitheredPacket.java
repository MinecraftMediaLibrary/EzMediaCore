package rewrite.pipeline.frame;

public interface DitheredPacket extends FramePacket {
  byte[] getMapDitheredSamples();
}
