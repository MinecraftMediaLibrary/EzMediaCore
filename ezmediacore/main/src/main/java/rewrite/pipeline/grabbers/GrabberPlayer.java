package rewrite.pipeline.grabbers;

import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.input.Input;

import java.util.Collection;
import java.util.Map;

public interface GrabberPlayer<O> extends FrameGrabberSource<O> {

  void play(final Input source, final Map<String, String> arguments);

  void play(final Input video, final Input audio, final Map<String, String> arguments);

  void resume();

  void pause();

  void seek(final long position);

  void release();

  Collection<Input> getSources();

  FramePipelineResult getPipeline();
}
