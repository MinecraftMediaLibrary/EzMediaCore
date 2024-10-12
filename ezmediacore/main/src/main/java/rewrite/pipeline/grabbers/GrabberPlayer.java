package rewrite.pipeline.grabbers;

import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.input.Input;

import java.util.Collection;

public interface GrabberPlayer<O> extends FrameGrabberSource<O> {

  void play(final Input source, final String[] arguments);

  void play(final Input video, final Input audio, final String[] arguments);

  void resume();

  void pause();

  void seek(final long position);

  void release();

  Collection<Input> getSources();

  FramePipelineResult getPipeline();
}
