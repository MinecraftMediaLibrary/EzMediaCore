package rewrite.pipeline.frame;

public interface FramePacket {

  int[] getRGBSamples();

  byte[] getAudioSamples();

  int getImageWidth();

  int getImageHeight();
}
