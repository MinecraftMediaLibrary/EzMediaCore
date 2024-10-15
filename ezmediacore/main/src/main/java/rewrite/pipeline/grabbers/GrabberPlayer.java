package rewrite.pipeline.grabbers;

import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.input.Input;

import java.util.Collection;
import java.util.Map;

public interface GrabberPlayer<O> extends FrameGrabberSource<O> {

  GrabberAudioFormat AUDIO_FORMAT = new GrabberAudioFormat("opus", "s16be", 48000, 16, 2, true, true);

  void play(final Input source, final Map<String, String> arguments);

  void play(final Input video, final Input audio, final Map<String, String> arguments);

  void resume();

  void pause();

  void seek(final long position);

  void release();

  Collection<Input> getSources();

  FramePipelineResult getPipeline();
}
