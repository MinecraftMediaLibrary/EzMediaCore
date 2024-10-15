package rewrite.util.graphics;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.nio.ShortBuffer;

public final class FrameUtils {

  private FrameUtils() {
  }

  public static byte[] getAudioSamples(final Frame captured) {
    final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) captured.samples[0];
    channelSamplesShortBuffer.rewind();
    final byte[] samples = new byte[channelSamplesShortBuffer.capacity() * 2];
    for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
      final short val = channelSamplesShortBuffer.get(i);
      samples[i * 2] = (byte) (val & 0xff);
      samples[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
    }
    return samples;
  }

  public static  int[] getRGBSamples(final int width, final int height, final Frame captured) {
    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Java2DFrameConverter.copy(image, captured);
    final int[] rgbSamples = new int[width * height];
    image.getRGB(0, 0, width, height, rgbSamples, 0, width);
    return rgbSamples;
  }
}
