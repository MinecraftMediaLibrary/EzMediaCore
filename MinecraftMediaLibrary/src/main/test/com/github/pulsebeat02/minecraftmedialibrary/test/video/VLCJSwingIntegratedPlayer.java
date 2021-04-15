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

package com.github.pulsebeat02.minecraftmedialibrary.test.video;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.FilterLiteDither;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries.
 */
public class VLCJSwingIntegratedPlayer {

  private final EmbeddedMediaPlayer mediaPlayerComponent;
  private final String url;

  public VLCJSwingIntegratedPlayer(@NotNull final File file) {
    mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
    url = file.getAbsolutePath();
    final BufferFormatCallback bufferFormatCallback =
        new BufferFormatCallback() {
          @Override
          public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
            return new RV32BufferFormat(640, 360);
          }

          @Override
          public void allocatedBuffers(final ByteBuffer[] buffers) {}
        };
    final CallbackVideoSurface surface =
        new CallbackVideoSurface(
            bufferFormatCallback,
            new SwingRenderCallback(),
            false,
            new WindowsVideoSurfaceAdapter());
    mediaPlayerComponent.videoSurface().set(surface);
    Logger.info(
        String.format("Created a VLCJ Integrated Video Player (%s)", file.getAbsolutePath()));
    start();
  }

  public static void main(final String[] args) {
    new VLCJSwingIntegratedPlayer(
        new File(String.format("%s/media/test.mp4", System.getProperty("user.dir"))));
  }

  public void start() {
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.release();
    }
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.media().play(url);
    }
    Logger.info(String.format("Started Playing Video! (%s)", url));
  }

  public void stop() {
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.controls().stop();
    }
  }

  /**
   * Gets media player component.
   *
   * @return the media player component
   */
  public EmbeddedMediaPlayer getMediaPlayerComponent() {
    return mediaPlayerComponent;
  }

  private static class SwingRenderCallback extends RenderCallbackAdapter {

    private final FilterLiteDither algorithm;
    private JLabel image;

    private SwingRenderCallback() {
      super(new int[640 * 360]);
      algorithm = new FilterLiteDither();
      final JFrame frame = new JFrame();
      image = new JLabel();
      final Container contentPane = frame.getContentPane();
      contentPane.setLayout(new FlowLayout());
      contentPane.add(image);
      frame.pack();
      frame.setVisible(true);
    }

    @Override
    protected void onDisplay(final MediaPlayer mediaPlayer, final int[] buffer) {
      algorithm.dither(buffer, 640);
      image = new JLabel(new ImageIcon(VideoUtilities.getBufferedImage(buffer, 640, 360)));
    }
  }
}
