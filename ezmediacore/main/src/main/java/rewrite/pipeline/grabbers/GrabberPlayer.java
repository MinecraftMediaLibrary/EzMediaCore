package rewrite.pipeline.grabbers;

import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.input.Input;

public interface GrabberPlayer<O> extends FrameGrabberSource<O> {

  void play(final Input source, final String[] arguments);

  void resume();

  void pause();

  void seek(final long position);

  void release();

  Input getSource();

  FramePipelineResult getPipeline();
}
