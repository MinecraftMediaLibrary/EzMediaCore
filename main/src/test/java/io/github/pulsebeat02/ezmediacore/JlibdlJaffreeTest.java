package io.github.pulsebeat02.ezmediacore;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.jlibdl.JLibDL;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JlibdlJaffreeTest {

  public static void main(final String @Nullable [] args) throws IOException, InterruptedException {
    final String url = "https://www.youtube.com/watch?v=_16io5rzmsQ&ab_channel=cantseetheforest";
    new FFmpeg(
            Path.of(
                "/Users/bli24/Desktop/ffmpeg/ffmpeg-x84_64-osx"))
        .addInput(UrlInput.fromUrl(new JLibDL().request(url).getInfo().getUrl()).setPosition(0))
        .addOutput(
            FrameOutput.withConsumer(
                    new FrameConsumer() {
                      @Override
                      public void consumeStreams(@NotNull final List<Stream> streams) {}

                      @Override
                      public void consume(@NotNull final Frame frame) {}
                    })
                .setFrameRate(30)
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA)
                .addArguments("-vf", "scale=640:360"))
        .setLogLevel(LogLevel.DEBUG)
        .setProgressListener((progress) -> {})
        .setOutputListener(JlibdlJaffreeTest::print)
        .execute();
  }

  private static void print(@NotNull final String line) {
    System.out.println(line);
  }
}
