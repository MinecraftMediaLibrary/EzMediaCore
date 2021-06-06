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

package io.github.pulsebeat02.minecraftmedialibrary.extraction;

import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegDependencyInstallation;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractionTest {

  public static void main(final String[] args) throws IOException {

    final Path parent = Paths.get(System.getProperty("user.dir"));
    final Path path = parent.resolve("ffmpeg-test");
    final Path audio = path.resolve("audio.ogg");
    if (Files.exists(audio)) {
      Files.delete(audio);
    }
    if (Files.notExists(path)) {
      Files.createDirectory(path);
    }

    new FFmpegDependencyInstallation(path).start();

    System.out.println(FFmpegDependencyInstallation.getFFmpegPath());

    final ExtractionSetting settings = new ExtractionSetting("libvorbis", 160000, 2, 44100, 100);
    new YoutubeExtraction(
            "https://www.youtube.com/watch?v=NFB6Y2Qt8ag&list=LL&index=5", path, settings)
        .extractAudio();
  }
}
