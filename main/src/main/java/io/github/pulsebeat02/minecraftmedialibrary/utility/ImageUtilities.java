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

package io.github.pulsebeat02.minecraftmedialibrary.utility;

// import com.sun.imageio.plugins.gif.GIFImageReader;
// import com.sun.imageio.plugins.gif.GIFImageReaderSpi;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.filters.ScaleFilter;
import ws.schild.jave.filters.helpers.ForceOriginalAspectRatio;
import ws.schild.jave.info.VideoSize;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A series of Image utility functions that are used to extract information about certain image
 * files.
 *
 * @deprecated uses sun packages
 */
@Deprecated
public final class ImageUtilities {

  private ImageUtilities() {}

  /**
   * Gets the frames of the gif file.
   *
   * @param gif the gif file
   * @return a List of BufferedImage's containing the frames
   */
  public static List<BufferedImage> getFrames(@NotNull final Path gif) {
    final List<BufferedImage> frames = new ArrayList<>();
    /*
    final ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
    try {
      ir.setInput(ImageIO.createImageInputStream(gif));
      for (int i = 0; i < ir.getNumImages(true); i++) {
        frames.add(ir.read(i));
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    */
    return frames;
  }

  /**
   * Gets the delay between each frame in the gif.
   *
   * @param file the gif file
   * @return the delay between each frame
   */
  public static float getGifFrameDelay(@NotNull final Path file) {
    try {
      final RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r");
      raf.seek(324);
      return raf.read() / 100f;
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return -1f;
  }

  public static void convertGifToMpeg(@NotNull final Path gif, @NotNull final Path output)
      throws IOException {
    if (!FilenameUtils.getExtension(PathUtilities.getName(gif)).equalsIgnoreCase("gfi")) {
      throw new IOException(
          String.format("Invalid Image Format (Must be Gif) %s", gif.toAbsolutePath()));
    }
    final AudioAttributes audio = new AudioAttributes();
    audio.setVolume(0);
    final VideoAttributes vAttrs = new VideoAttributes();
    vAttrs.setPixelFormat("yuv420p");
    vAttrs.addFilter(new ScaleFilter(VideoSize.vga, ForceOriginalAspectRatio.DISABLE));
    final EncodingAttributes attrs = new EncodingAttributes();
    attrs.setAudioAttributes(audio);
    attrs.setVideoAttributes(vAttrs);
    attrs.setOutputFormat("mp4");
    try {
      final Encoder encoder = new Encoder();
      encoder.encode(new MultimediaObject(gif.toFile()), output.toFile(), attrs);
    } catch (final EncoderException e) {
      e.printStackTrace();
    }
  }
}
