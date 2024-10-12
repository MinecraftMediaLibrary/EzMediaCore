package rewrite.pipeline.frame;

public final class BasicPacket implements FramePacket {

  private final int[] rgbSamples;
  private final byte[] audioSamples;
  private final int width;
  private final int height;

  public BasicPacket(final int[] rgbSamples, final byte[] audioSamples
      , final int width, final int height) {
    this.rgbSamples = rgbSamples;
    this.audioSamples = audioSamples;
    this.width = width;
    this.height = height;
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
}
