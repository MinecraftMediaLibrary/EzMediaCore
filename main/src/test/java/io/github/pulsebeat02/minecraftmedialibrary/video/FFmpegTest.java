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

package io.github.pulsebeat02.minecraftmedialibrary.video;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameInput;
import com.github.kokorin.jaffree.ffmpeg.FrameProducer;
import com.github.kokorin.jaffree.ffmpeg.Stream;

import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.Collections;

public class FFmpegTest {
  public static void main(final String[] args) {
    final FrameProducer producer = new FrameProducer() {
      private long frameCounter = 0;
      @Override
      public java.util.List<Stream> produceStreams() {
        return Collections.singletonList(new Stream()
                .setType(Stream.Type.VIDEO)
                .setTimebase(1000L)
                .setWidth(320)
                .setHeight(240)
        );
      }
      @Override
      public Frame produce() {
        if (frameCounter > 30) {
          return null;
        }
        final BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new Color(frameCounter * 1.0f / 30, 0, 0));
        graphics.fillRect(0, 0, 320, 240);
        final Frame videoFrame = Frame.createVideoFrame(0, frameCounter * 100, image);
        frameCounter++;
        return videoFrame;
      }
    };
    new FFmpeg(
            Paths.get(
                "C:\\Users\\Brandon Li\\Desktop\\server\\plugins\\DeluxeMediaPlugin\\mml\\libraries\\ffmpeg\\ffmpeg-amd64.exe"))
        .addInput(FrameInput.withProducer(producer))
        .addOutput(UrlOutput.toUrl("C://test.mp4"))
        .execute();
  }
}
