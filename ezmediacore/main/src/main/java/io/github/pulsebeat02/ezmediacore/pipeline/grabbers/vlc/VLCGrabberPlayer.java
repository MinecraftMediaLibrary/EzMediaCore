package io.github.pulsebeat02.ezmediacore.pipeline.grabbers.vlc;

import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.pipeline.FramePipelineResult;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.BasicFramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.grabbers.GrabberAudioFormat;
import io.github.pulsebeat02.ezmediacore.pipeline.grabbers.GrabberPlayer;
import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.util.os.OSUtils;
import uk.co.caprica.vlcj.factory.MediaPlayerApi;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaSlaveType;
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

public final class VLCGrabberPlayer implements GrabberPlayer<FramePacket> {

  private final FramePipelineResult result;
  private final Collection<Input> sources;

  private MediaPlayerFactory factory;
  private EmbeddedMediaPlayer player;
  private volatile int width;
  private volatile int height;
  private volatile int[] frames;
  private volatile byte[] audioFrames;

  public VLCGrabberPlayer(final FramePipelineResult result) {
    final MediaPlayerFactory factory = new MediaPlayerFactory();
    final MediaPlayerApi api = factory.mediaPlayers();
    this.factory = factory;
    this.player = api.newEmbeddedMediaPlayer();
    this.sources = new ArrayList<>();
    this.result = result;
    this.setPlayerSurfaces();
    this.setPlayerCodec();
  }

  private void setPlayerSurfaces() {
    final BufferFormatCallback callback = new VLCVideoCallback();
    final RenderCallbackAdapter callbackAdapter = new VLCVideoRenderCallback();
    final VideoSurfaceAdapter adapter = this.getAdapter();
    final VideoSurface surface = new CallbackVideoSurface(callback, callbackAdapter, true, adapter);
    final VideoSurfaceApi surfaceApi = this.player.videoSurface();
    surfaceApi.set(surface);
  }

  private void setPlayerCodec() {
    final AudioCallback audioCallback = new VLCAudioCallback();
    final AudioApi api = this.player.audio();
    final GrabberAudioFormat standard = GrabberPlayer.AUDIO_FORMAT;
    final String format = standard.getFormat();
    final int sampleRate = standard.getSampleRate();
    final int channels = standard.getChannels();
    api.callback(format, sampleRate, channels, audioCallback);
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    this.play(source, null, arguments);
  }

  private VideoSurfaceAdapter getAdapter() {
    return switch (OSUtils.getOS()) {
      case OSX -> new OsxVideoSurfaceAdapter();
      case UNIX -> new LinuxVideoSurfaceAdapter();
      case WIN -> new WindowsVideoSurfaceAdapter();
      default -> throw new UnsupportedOperationException();
    };
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {
    final MediaApi api = this.player.media();
    final CompletableFuture<String> videoFuture = video.getMediaRepresentation();
    final String videoUrl = videoFuture.join();
    this.sources.clear();
    if (audio != null) {
      final CompletableFuture<String> audioFuture = audio.getMediaRepresentation();
      final String audioUrl = audioFuture.join();
      api.addSlave(MediaSlaveType.AUDIO, audioUrl, true);
      this.sources.add(audio);
    }
    this.sources.add(video);
    final String[] args = getArguments(arguments);
    api.play(videoUrl, args);
  }

  private static String[] getArguments(final Map<String, String> arguments) {
    final Map<String, String> copy = new HashMap<>(arguments);
    final int size = arguments.size() * 2 + 1;
    final String[] args = new String[size];
    final Set<Map.Entry<String, String>> entries = copy.entrySet();
    int index = 0;
    for (final Map.Entry<String, String> entry : entries) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      args[index++] = key;
      args[index++] = value;
    }
    final int last = size - 1;
    args[last] = ":sout=#transcode{acodec=opus}";
    return args;
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
    return new BasicFramePacket(this.frames, this.audioFrames, this.width, this.height, null);
  }



  private final class VLCAudioCallback extends AudioCallbackAdapter {

    @Override
    public void play(
            final MediaPlayer mediaPlayer,
            final Pointer samples,
            final int sampleCount,
            final long pts) {
      VLCGrabberPlayer.this.audioFrames = samples.getByteArray(0, sampleCount);
    }
  }

  private final class VLCVideoCallback implements BufferFormatCallback {

    @Override
    public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
      VLCGrabberPlayer.this.width = sourceWidth;
      VLCGrabberPlayer.this.height = sourceHeight;
      return new RV32BufferFormat(sourceWidth, sourceHeight);
    }

    @Override
    public void allocatedBuffers(final ByteBuffer[] buffers) {}
  }

  private final class VLCVideoRenderCallback extends RenderCallbackAdapter {

    @Override
    protected void onDisplay(
            final MediaPlayer mediaPlayer, final int[] buffer) {
      VLCGrabberPlayer.this.frames = buffer;
    }
  }
}
