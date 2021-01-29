package com.github.pulsebeat02.video.player;

import com.github.pulsebeat02.MinecraftMediaLibrary;
import com.github.pulsebeat02.extractor.YoutubeExtraction;
import com.github.pulsebeat02.utility.VideoUtilities;
import com.github.pulsebeat02.video.dither.AbstractDitherHolder;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

public class BasicVideoPlayer extends AbstractVideoPlayer {

    private final FFmpegFrameGrabber grabber;
    private final File video;

    private volatile boolean stopped;
    private Thread videoThread;

    public BasicVideoPlayer(@NotNull final MinecraftMediaLibrary library,
                            @NotNull final File video,
                            final int width, final int height,
                            @NotNull final Consumer<int[]> callback) {
        super(library, video.getAbsolutePath(), width, height, callback);
        this.grabber = new FFmpegFrameGrabber(video);
        this.video = video;
    }

    public BasicVideoPlayer(@NotNull final MinecraftMediaLibrary library,
                            @NotNull final String url,
                            final int width, final int height,
                            @NotNull final Consumer<int[]> callback) {
        super(library, url, width, height, callback);
        File f = new YoutubeExtraction(url, getLibrary().getPath()).downloadVideo();
        this.grabber = new FFmpegFrameGrabber(f);
        this.video = f;
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
                    getCallback().accept(VideoUtilities.getBuffer(Java2DFrameUtils.toBufferedImage(grabber.grab())));
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
//                try {
//                    int[] buffer = VideoUtilities.getBuffer(Java2DFrameUtils.toBufferedImage(grabber.grab()));
//                    library.getHandler().display(viewers, map, getWidth(), getHeight(), type.ditherIntoMinecraft(buffer, width), width);
//                    Thread.sleep(5);
//                } catch (FrameGrabber.Exception | InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
        videoThread.start();
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public class Builder {

        private File video;
        private int map;
        private int width;
        private int height;
        private Consumer<int[]> callback;

        public Builder setVideo(@NotNull final File video) {
            this.video = video;
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

        public Builder setHolder(@NotNull final Consumer<int[]> callback) {
            this.callback = callback;
            return this;
        }

        public BasicVideoPlayer createBasicVideoPlayer(@NotNull final MinecraftMediaLibrary library) {
            return new BasicVideoPlayer(library, video, width, height, callback);
        }

    }

    public FFmpegFrameGrabber getGrabber() {
        return grabber;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Thread getVideoThread() {
        return videoThread;
    }

    public File getVideo() {
        return video;
    }

}
