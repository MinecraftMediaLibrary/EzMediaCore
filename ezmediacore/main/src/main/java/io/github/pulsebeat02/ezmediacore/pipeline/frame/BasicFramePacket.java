package io.github.pulsebeat02.ezmediacore.pipeline.frame;

public class BasicFramePacket implements FramePacket {

  private final int[] rgbSamples;
  private final byte[] audioSamples;
  private final int width;
  private final int height;
  private final Object metadata;

  public BasicFramePacket(final int[] rgbSamples, final byte[] audioSamples
      , final int width, final int height, final Object metadata) {
    this.rgbSamples = rgbSamples;
    this.audioSamples = audioSamples;
    this.width = width;
    this.height = height;
    this.metadata = metadata;
  }

  @Override
  public int[] getRGBSamples() {
    return this.rgbSamples;
  }

  @Override
  public byte[] getAudioSamples() {
    return this.audioSamples;
  }

  @Override
  public int getImageWidth() {
    return this.width;
  }

  @Override
  public int getImageHeight() {
    return this.height;
  }

  @Override
  public Object getMetadata() {
    return null;
  }
}
