package rewrite.pipeline.grabbers;

import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.utility.misc.OSType;
import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.frame.BasicFramePacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.input.Input;
import uk.co.caprica.vlcj.factory.MediaPlayerApi;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.AudioApi;
import uk.co.caprica.vlcj.player.base.ControlsApi;
import uk.co.caprica.vlcj.player.base.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.callback.AudioCallback;
import uk.co.caprica.vlcj.player.base.callback.AudioCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.VideoSurfaceApi;
import uk.co.caprica.vlcj.player.embedded.videosurface.*;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class VLCGrabberSource implements GrabberPlayer<FramePacket> {

  private final FramePipelineResult result;
  private final ExecutorService executor;
  private final Queue<int[]> frames;
  private final Queue<byte[]> audioFrames;
  private final Collection<Input> sources;

  private MediaPlayerFactory factory;
  private EmbeddedMediaPlayer player;
  private volatile int width;
  private volatile int height;

  public VLCGrabberSource(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public VLCGrabberSource(final FramePipelineResult result, final ExecutorService executor) {
    final MediaPlayerFactory factory = new MediaPlayerFactory();
    final MediaPlayerApi api = factory.mediaPlayers();
    this.factory = factory;
    this.player = api.newEmbeddedMediaPlayer();
    this.frames = new LinkedList<>();
    this.audioFrames = new LinkedList<>();
    this.sources = new ArrayList<>();
    this.result = result;
    this.executor = executor;
    this.setPlayerSurfaces();
  }

  private void setPlayerSurfaces() {
    final BufferFormatCallback callback = new MinecraftVideoCallback();
    final RenderCallbackAdapter callbackAdapter = new MinecraftVideoRenderCallback();
    final VideoSurfaceAdapter adapter = this.getAdapter();
    final VideoSurface surface = new CallbackVideoSurface(callback, callbackAdapter, true, adapter);
    final VideoSurfaceApi surfaceApi = this.player.videoSurface();
    final AudioApi api = this.player.audio();
    final AudioCallback audioCallback = new MinecraftAudioCallback();
    surfaceApi.set(surface);
    api.callback("", 160000, 2, audioCallback);
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    final MediaApi api = this.player.media();
    final String[] args = arguments.values().toArray(new String[0]);
    final CompletableFuture<String> future = source.getMediaRepresentation();
    final String result = future.join();
    this.sources.clear();
    this.sources.add(source);
    api.play(result, args);
  }

  private VideoSurfaceAdapter getAdapter() {
    return switch (OSType.getCurrentOS()) {
      case MAC -> new OsxVideoSurfaceAdapter();
      case UNIX -> new LinuxVideoSurfaceAdapter();
      case WINDOWS -> new WindowsVideoSurfaceAdapter();
    };
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {

  }

  @Override
  public void resume() {
    if (this.player != null) {
      final ControlsApi api = this.player.controls();
      api.play();
    }
  }

  @Override
  public void pause() {
    if (this.player != null) {
      final ControlsApi api = this.player.controls();
      api.pause();
    }
  }

  @Override
  public void seek(final long position) {
    if (this.player != null) {
      final ControlsApi api = this.player.controls();
      api.setTime(position);
    }
  }

  @Override
  public void release() {
    if (this.player != null) {
      this.player.controls().stop();
      this.player.release();
      this.factory.release();
      this.player = null;
      this.factory = null;
    }
  }

  @Override
  public Collection<Input> getSources() {
    return this.sources;
  }

  @Override
  public FramePipelineResult getPipeline() {
    return this.result;
  }

  @Override
  public FramePacket grabOutputFrame() {
    final int[] frame = this.frames.poll();
    final byte[] audioFrame = this.audioFrames.poll();
    return new BasicFramePacket(frame, audioFrame, this.width, this.height, null);
  }

  private final class MinecraftAudioCallback extends AudioCallbackAdapter {

    @Override
    public void play(
            final MediaPlayer mediaPlayer,
            final Pointer samples,
            final int sampleCount,
            final long pts) {
      final byte[] arr = samples.getByteArray(0, sampleCount);
      VLCGrabberSource.this.audioFrames.add(arr);
    }
  }

  private final class MinecraftVideoCallback implements BufferFormatCallback {

    @Override
    public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
      VLCGrabberSource.this.width = sourceWidth;
      VLCGrabberSource.this.height = sourceHeight;
      return new RV32BufferFormat(sourceWidth, sourceHeight);
    }

    @Override
    public void allocatedBuffers(final ByteBuffer[] buffers) {}
  }

  private final class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    @Override
    protected void onDisplay(
            final MediaPlayer mediaPlayer, final int[] buffer) {
      VLCGrabberSource.this.frames.add(buffer);
    }
  }
}
