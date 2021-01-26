package com.github.pulsebeat02.video.vlcj;

import com.github.pulsebeat02.logger.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class VLCJIntegratedPlayer {

    private final String url;
    private final int width;
    private final int height;
    private final Consumer<int[]> callback;

    private EmbeddedMediaPlayer mediaPlayerComponent;

    public VLCJIntegratedPlayer(@NotNull final String url,
                                final int width,
                                final int height,
                                @NotNull final Consumer<int[]> callback) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.callback = callback;
        Logger.info("Created a VLCJ Integrated Video Player (" + url + ")");
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void start() {
        if (mediaPlayerComponent != null) {
            mediaPlayerComponent.release();
        }
        mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
        BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                return new RV32BufferFormat(width, height);
            }

            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {
            }
        };
        CallbackVideoSurface surface = new CallbackVideoSurface(bufferFormatCallback, new MinecraftRenderCallback(), false, new WindowsVideoSurfaceAdapter());
        mediaPlayerComponent.videoSurface().set(surface);
        mediaPlayerComponent.media().play(url);
        Logger.info("Started Playing Video! (" + url + ")");
    }

    public void stop() {
        if (mediaPlayerComponent != null) {
            mediaPlayerComponent.controls().stop();
            Logger.info("Stopped Playing Video! (" + url + ")");
        }
    }

    private class MinecraftRenderCallback extends RenderCallbackAdapter {

        private MinecraftRenderCallback() {
            super(new int[width * height]);
        }

        @Override
        protected void onDisplay(final MediaPlayer mediaPlayer, final int[] buffer) {
            callback.accept(buffer);
        }

    }

    public Consumer<int[]> getCallback() {
        return callback;
    }

    public EmbeddedMediaPlayer getMediaPlayerComponent() {
        return mediaPlayerComponent;
    }

}
