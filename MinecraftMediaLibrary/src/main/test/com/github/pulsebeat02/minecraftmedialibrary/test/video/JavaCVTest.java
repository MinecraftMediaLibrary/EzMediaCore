/*
 * ============================================================================
 *  Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 *  This file is part of MinecraftMediaLibrary
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *
 *  Written by Brandon Li <brandonli2006ma@gmail.com>, 2/12/2021
 * ============================================================================
 */

/*
package com.github.pulsebeat02.minecraftmedialibrary.test;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class JavaCVTest implements Runnable {

  static CanvasFrame canvas = new CanvasFrame("JavaCV player");

  public JavaCVTest() {
    canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
  }

  public static void main(final String[] args) {
    final JavaCVTest gs = new JavaCVTest();
    final Thread th = new Thread(gs);
    th.start();
  }

  public void convert(@NotNull final File file) {
    final FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());
    Frame frame;
    final int delay = (int) (1000 * (frameGrabber.getVideoFrameRate()));
    try {
      frameGrabber.start();
      while (true) {
        try {
          frame = frameGrabber.grab();
          if (frame == null) {
            break;
          }
          canvas.showImage(frame);
          wait(delay);
        } catch (final Exception ignored) {
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    convert(new File("C:\\Users\\Brandon Li\\Videos\\2021-01-18 22-09-42.mkv"));
  }
}
*/
