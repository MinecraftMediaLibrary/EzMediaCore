package com.github.pulsebeat02.video;

import com.github.pulsebeat02.MinecraftMediaLibrary;
import com.github.pulsebeat02.utility.VideoUtilities;
import com.github.pulsebeat02.video.dither.AbstractDitherHolder;
import com.github.pulsebeat02.video.dither.FloydImageDither;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

public class BasicVideoPlayer {

    private final MinecraftMediaLibrary library;
    private final FFmpegFrameGrabber grabber;
    private final AbstractDitherHolder type;
    private final File video;
    private final UUID[] viewers;
    private final int map;
    private final int width;
    private final int height;
    private final int videoWidth;
    private final int delay;

    private volatile boolean stopped;
    private Thread videoThread;

    public BasicVideoPlayer(@NotNull final MinecraftMediaLibrary library,
                            @NotNull final File video,
                            @NotNull final UUID[] viewers,
                            final int map,
                            final int width, final int height, final int videoWidth,
                            final int delay,
                            final AbstractDitherHolder holder) {
        this.library = library;
        this.grabber = new FFmpegFrameGrabber(video);
        this.type = holder;
        this.video = video;
        this.viewers = viewers;
        this.map = map;
        this.width = width;
        this.height = height;
        this.videoWidth = videoWidth;
        this.delay = delay;
    }

    public void start() throws FrameGrabber.Exception {
        grabber.start();
        videoThread = new Thread(() -> {
            for (int i = 0; i < grabber.getLengthInVideoFrames() && !stopped; i++) {
                try {
                    int[] buffer = VideoUtilities.getBuffer(Java2DFrameUtils.toBufferedImage(grabber.grab()));
                    library.getHandler().display(viewers, map, width, height, type.ditherIntoMinecraft(buffer, videoWidth), videoWidth);
                    Thread.sleep(delay);
                } catch (FrameGrabber.Exception | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        videoThread.start();
    }

    public void stop() {
        stopped = true;
    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

    public FFmpegFrameGrabber getGrabber() {
        return grabber;
    }

    public File getVideo() {
        return video;
    }

    public UUID[] getViewers() {
        return viewers;
    }

    public int getMap() {
        return map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Thread getVideoThread() {
        return videoThread;
    }

}
