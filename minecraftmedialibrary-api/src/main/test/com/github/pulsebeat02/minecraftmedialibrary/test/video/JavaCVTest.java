package com.github.pulsebeat02.minecraftmedialibrary.test.video;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class JavaCVTest {

  public static void main(final String[] args) throws Exception {
    final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(1);
    grabber.start();
    final CanvasFrame cFrame =
        new CanvasFrame("Capture Preview", CanvasFrame.getDefaultGamma() / grabber.getGamma());
    Frame frame;
    while ((frame = grabber.grab()) != null) {
      if (cFrame.isVisible()) {
        final byte[] buffer = frame.data.array();
      }
    }
    cFrame.dispose();
    grabber.stop();
  }
}
