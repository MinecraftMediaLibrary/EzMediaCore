package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.jcodec.codecs.mjpeg.tools.AssertionException;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.LinuxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.OsxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class VLCMediaPlayer extends MediaPlayer {

  private final VideoSurfaceAdapter adapter;
  private final MinecraftVideoRenderCallback callback;

  private EmbeddedMediaPlayer player;

  public VLCMediaPlayer(
      @NotNull final MediaLibraryCore core,
      @NotNull final FrameCallback callback,
      @NotNull final ImmutableDimension dimensions,
      @NotNull final String url,
      final int frameRate) {
    super(core, callback, dimensions, url, frameRate);
    this.adapter = getAdapter();
    this.callback = new MinecraftVideoRenderCallback(this);
    initializePlayer(0L);
  }

  private VideoSurfaceAdapter getAdapter() {
    switch (getCore().getDiagnostics().getSystem().getOSType()) {
      case MAC:
        return new OsxVideoSurfaceAdapter();
      case UNIX:
        return new LinuxVideoSurfaceAdapter();
      case WINDOWS:
        return new WindowsVideoSurfaceAdapter();
    }
    throw new AssertionException("Invalid Operating System!");
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START:
        if (this.player == null) {
          initializePlayer(0L);
        }
        this.player.media().play(getUrl());
        playAudio();
        break;
      case PAUSE:
        this.player.controls().stop();
        stopAudio();
        break;
      case RESUME:
        if (this.player == null) {
          initializePlayer(0L);
          this.player.media().play(getUrl());
        } else {
          this.player.controls().play();
        }
        playAudio();
        break;
      case RELEASE:
        this.player.release();
        this.player = null;
        break;
    }
  }

  @Override
  public void initializePlayer(final long ms) {
    this.player = getEmbeddedMediaPlayer();
    setCallback(this.player);
    this.player.audio().setMute(true);
    this.player.controls().setTime(ms);

    // TODO: 7/30/2021 resourcepack stuff
  }

  private EmbeddedMediaPlayer getEmbeddedMediaPlayer() {
    final int rate = getFrameRate();
    return new MediaPlayerFactory(
            rate != 0 ? new String[] {String.format("--fps-fps=%d", rate)} : new String[] {})
        .mediaPlayers()
        .newEmbeddedMediaPlayer();
  }

  private void setCallback(@NotNull final EmbeddedMediaPlayer player) {
    player.videoSurface().set(getSurface());
  }

  private CallbackVideoSurface getSurface() {
    return new CallbackVideoSurface(getBufferCallback(), this.callback, false, this.adapter);
  }

  private BufferFormatCallback getBufferCallback() {
    return new BufferFormatCallback() {
      @Override
      public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
        final ImmutableDimension dimension = getDimensions();
        return new RV32BufferFormat(dimension.getWidth(), dimension.getHeight());
      }

      @Override
      public void allocatedBuffers(final ByteBuffer[] buffers) {}
    };
  }

  private static class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final Consumer<int[]> callback;

    public MinecraftVideoRenderCallback(@NotNull final VLCMediaPlayer player) {
      super(new int[player.getDimensions().getWidth() * player.getDimensions().getHeight()]);
      this.callback = player.getCallback()::process;
    }

    @Override
    protected void onDisplay(
        final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, final int[] buffer) {
      this.callback.accept(buffer);
    }
  }
}
