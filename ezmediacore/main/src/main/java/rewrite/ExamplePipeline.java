package rewrite;

import rewrite.dimension.BlockDimension;
import rewrite.dimension.Dimension;
import rewrite.dimension.Resolution;
import rewrite.dither.load.DefaultPalette;
import rewrite.pipeline.FramePipeline;
import rewrite.pipeline.BasicPipelineResult;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.grabbers.ffmpeg.FFmpegGrabberPlayer;
import rewrite.pipeline.input.Input;
import rewrite.pipeline.input.parser.URLInputParser;
import rewrite.pipeline.input.parser.strategy.DefaultAudioStrategy;
import rewrite.pipeline.input.parser.strategy.DefaultVideoStrategy;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.Identifier;
import rewrite.pipeline.output.Viewers;
import rewrite.pipeline.output.video.MapFrameOutput;
import rewrite.pipeline.steps.dithering.FloydSteinbergDitherPipelineStep;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ExamplePipeline {

  public void createPipeline() {

    // merge with --merge-output-format (yt-dlp)

    final DefaultPalette palette = new DefaultPalette();
    final FloydSteinbergDitherPipelineStep step = new FloydSteinbergDitherPipelineStep(palette);

    final EzMediaCore core = null;
    final Dimension blockDimension = BlockDimension.X5_5;
    final Resolution resolution = Resolution.X360_640;
    final Viewers onlinePlayers = Viewers.onlinePlayers();
    final DelayConfiguration delay = DelayConfiguration.DELAY_20_MS;
    final Identifier<Integer> startingMap = Identifier.ofIdentifier(0);
    final MapFrameOutput output = new MapFrameOutput(core, onlinePlayers, delay, resolution, blockDimension, startingMap);

    final BasicPipelineResult result = FramePipeline.source(FramePacket.class)
            .thenPipe(step)
            .thenFinally(output);

    final FFmpegGrabberPlayer player = new FFmpegGrabberPlayer(result);
    final URLInputParser parser = new URLInputParser("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    final CompletableFuture<Input> video = parser.retrieveInput(new DefaultVideoStrategy());
    final CompletableFuture<Input> audio = parser.retrieveInput(new DefaultAudioStrategy());

    final Map<String, String> arguments = resolution.getFFMPEGArguments();
    player.play(video.join(), audio.join(), arguments);
  }
}
