package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.dimension.BlockDimension;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.dimension.Resolution;
import io.github.pulsebeat02.ezmediacore.dither.palette.DefaultPalette;
import io.github.pulsebeat02.ezmediacore.pipeline.FramePipeline;
import io.github.pulsebeat02.ezmediacore.pipeline.BasicPipelineResult;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.grabbers.ffmpeg.FFmpegGrabberPlayer;
import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.pipeline.input.URLParsedInput;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.URLInputParser;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy.DefaultAudioStrategy;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy.DefaultVideoStrategy;
import io.github.pulsebeat02.ezmediacore.pipeline.output.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.pipeline.output.Identifier;
import io.github.pulsebeat02.ezmediacore.pipeline.output.Viewers;
import io.github.pulsebeat02.ezmediacore.pipeline.output.video.MapFrameOutput;
import io.github.pulsebeat02.ezmediacore.pipeline.steps.dithering.FloydSteinbergDitherPipelineStep;
import io.github.pulsebeat02.ezmediacore.resourcepack.provider.MCPacksHosting;
import io.github.pulsebeat02.ezmediacore.resourcepack.wrapper.PackCreator;
import io.github.pulsebeat02.ezmediacore.resourcepack.wrapper.SoundMedia;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ExamplePipelineTest {

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

    player.setResolution(resolution);
    player.play(video.join(), audio.join());
  }

  private void createResourcePack() {
    final URLInputParser parser = new URLInputParser("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    final URLParsedInput input = new URLParsedInput(parser, false);
    final CompletableFuture<SoundMedia> media = SoundMedia.encodeToVorbisOGG(input);
    final PackCreator creator = PackCreator.create(Path.of("output.zip"), media.join());
    final Path pack = creator.writePack();
    final MCPacksHosting hosting = new MCPacksHosting();
    final String url = hosting.uploadPack(pack);
  }
}
