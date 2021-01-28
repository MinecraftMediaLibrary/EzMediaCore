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

public class BasicVideoPlayer implements AbstractVideoPlayer {

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
                            @NotNull final AbstractDitherHolder holder) {
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

    @Override
    public void start() {
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void stop() {
        stopped = true;
    }

    public class Builder {

        private MinecraftMediaLibrary library;
        private File video;
        private UUID[] viewers;
        private int map;
        private int width;
        private int height;
        private int videoWidth;
        private int delay;
        private AbstractDitherHolder holder;

        public Builder setLibrary(@NotNull final MinecraftMediaLibrary library) {
            this.library = library;
            return this;
        }

        public Builder setVideo(@NotNull final File video) {
            this.video = video;
            return this;
        }

        public Builder setViewers(@NotNull final UUID[] viewers) {
            this.viewers = viewers;
            return this;
        }

        public Builder setMap(final int map) {
            this.map = map;
            return this;
        }

        public Builder setWidth(final int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(final int height) {
            this.height = height;
            return this;
        }

        public Builder setVideoWidth(final int videoWidth) {
            this.videoWidth = videoWidth;
            return this;
        }

        public Builder setDelay(final int delay) {
            this.delay = delay;
            return this;
        }

        public Builder setHolder(@NotNull final AbstractDitherHolder holder) {
            this.holder = holder;
            return this;
        }

        public BasicVideoPlayer createBasicVideoPlayer() {
            return new BasicVideoPlayer(library, video, viewers, map, width, height, videoWidth, delay, holder);
        }

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
