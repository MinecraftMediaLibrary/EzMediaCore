package rewrite.pipeline.steps;

import rewrite.pipeline.frame.FramePacket;

import java.awt.image.BufferedImage;

public final class BufferedImagePipelineStep implements FramePipelineStep<FramePacket, BufferedImage> {

  @Override
  public BufferedImage process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final int height = input.getImageHeight();
    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    image.setRGB(0, 0, width, height, rgb, 0, width);
    return image;
  }
}
