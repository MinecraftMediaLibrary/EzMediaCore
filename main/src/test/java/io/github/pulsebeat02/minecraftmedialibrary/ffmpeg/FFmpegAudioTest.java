/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.ffmpeg;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.PipeOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.minecraftmedialibrary.dependency.FFmpegDependencyInstallation;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FFmpegAudioTest {

  public static void main(final String[] args) throws IOException {
    final Path parent = Paths.get(System.getProperty("user.dir"));
    final Path path = parent.resolve("ffmpeg-test");
    final Path audioPath = parent.resolve("audio.ogg");
    if (Files.notExists(path)) {
      Files.createDirectory(path);
    }

    final ExtractionSetting setting = new ExtractionSetting(160000, 2, 44100, 1);
    final PipeOutput output = PipeOutput.pumpTo(Files.newOutputStream(audioPath));
    output.setCodec(StreamType.AUDIO, setting.getCodec());
    output.addArguments("-b:v", String.valueOf(setting.getBitrate() / 1000F));
    output.addArguments("-ac", "2");
    output.addArguments("-r", String.valueOf(setting.getSamplingRate()));
    output.addArguments("-filter:a", "\"volume=" + setting.getVolume() + "\"");

    new FFmpegDependencyInstallation(path).start();
    new FFmpeg(Paths.get(FFmpegDependencyInstallation.getFFmpegPath()))
        .addInput(UrlInput.fromPath(Paths.get("/Users/bli24/Downloads/opsu/Songs/1034063 K-DA - POP-STARS (feat. Madison Beer, (G)I-DLE, Jaira Burns)/audio.mp3")))
        .addOutput(output)
        .execute();
  }
}
