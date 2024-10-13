package rewrite.pipeline.frame;

public final class DitheredFramePacket extends BasicPacket {

  private final byte[] mapDitheredSamples;

  public DitheredFramePacket(
          final int[] rgbSamples,
          final byte[] audioSamples,
          final int width,
          final int height,
          final Object javaCVFrame,
          final byte[] mapDitheredSamples) {
    super(rgbSamples, audioSamples, width, height, javaCVFrame);
    this.mapDitheredSamples = mapDitheredSamples;
  }

  public static DitheredFramePacket create(final FramePacket packet, final byte[] dithered) {
    final int[] rgbSamples = packet.getRGBSamples();
    final byte[] audioSamples = packet.getAudioSamples();
    final int imageWidth = packet.getImageWidth();
    final int imageHeight = packet.getImageHeight();
    final Object javaCVFrame = packet.getJavaCVAudioFrame();
    return new DitheredFramePacket(rgbSamples, audioSamples, imageWidth, imageHeight, javaCVFrame, dithered);
  }

  public byte[] getMapDitheredSamples() {
    return this.mapDitheredSamples;
  }
}
