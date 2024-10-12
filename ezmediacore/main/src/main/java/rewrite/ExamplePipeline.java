package rewrite;

import rewrite.pipeline.FramePipeline;
import rewrite.pipeline.BasicPipelineResult;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.grabbers.FFmpegGrabberPlayer;
import rewrite.pipeline.input.URLInput;
import rewrite.pipeline.outputs.MapFrameOutput;
import rewrite.pipeline.steps.FloydSteinbergDitherPipelineStep;

public final class ExamplePipeline {

  public void createPipeline() {

    final BasicPipelineResult result = FramePipeline.source(FramePacket.class)
            .thenPipe(new FloydSteinbergDitherPipelineStep())
            .thenFinally(new MapFrameOutput(640, 480, 4, 4, 0));

    final FFmpegGrabberPlayer player = new FFmpegGrabberPlayer(result);
    final URLInput input = new URLInput("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    final String[] arguments = new String[] {};
    player.play(input, arguments);

  }

}
