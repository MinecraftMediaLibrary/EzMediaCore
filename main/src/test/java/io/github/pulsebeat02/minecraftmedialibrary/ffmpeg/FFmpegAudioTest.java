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

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FFmpegAudioTest {

  public static void main(final String[] args) throws IOException {
    tryExtraction();
  }

  public static void tryExtraction() {

    final Path parent = Paths.get(System.getProperty("user.dir"));
    final Path path = parent.resolve("ffmpeg-test");
    final Path audioPath = path.resolve("audio.ogg");
    if (Files.notExists(path)) {
      try {
        Files.createDirectory(path);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    final UrlOutput output = UrlOutput.toPath(audioPath);
    output.addArguments("-b:v", "160K");
    output.addArguments("-ac", "2");
    output.addArguments("-r", "44100");
    output.addArguments("-filter:a", "\"volume=1\"");

    new FFmpeg(
            Paths.get(
                "/Users/bli24/IdeaProjects/MinecraftMediaLibrary/ffmpeg-test/ffmpeg/ffmpeg-x86_64-osx"))
        .addInput(UrlInput.fromPath(Paths.get("/Users/bli24/Downloads/kda.mp4")))
        .addOutput(output)
        .execute();
  }
}
