package com.github.pulsebeat02.test;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.File;

public class JavaCVTest implements Runnable {

    static CanvasFrame canvas = new CanvasFrame("JavaCV player");

    public JavaCVTest() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    public void convert(File file) {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());
        Frame frame;
        int delay = (int)(1000 * (frameGrabber.getVideoFrameRate()));
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
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        convert(new File("C:\\Users\\Brandon Li\\Videos\\2021-01-18 22-09-42.mkv"));
    }

    public static void main(String[] args) {
        JavaCVTest gs = new JavaCVTest();
        Thread th = new Thread(gs);
        th.start();
    }

}
