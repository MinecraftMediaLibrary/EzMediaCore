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

package com.github.pulsebeat02.minecraftmedialibrary.frame.parallel;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.VideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.FilterLiteDither;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
/**
 *
 *
 * <pre>
 *
 * Suppose we had two separate threads:
 *
 * Thread #1: Dithers a specific frame.
 * Thread #2: Sends the dithered frame data to the clients.
 *
 * Frame # | What to do:
 * --------|-------------------------------------
 * 1       | Thread #1 dithers this frame.
 * 2       | Thread #2 sends out frame number 1, while Thread #1 dithers frame number 2.
 * 3       | Thread #2 sends out frame number 2, while Thread #1 dithers frame number 3.
 *
 * And so on, (chaining domino pattern)
 *
 * -------------------------------------------------------------------------------------
 * The issue however with one thread is blocking. If the dithered results aren't ready,
 * this means that thread #2 has to be blocked from thread #1 to wait for the send the
 * dithered results to clients. This will significantly slow the video speed and affect
 * the frames being sent per second.
 * -------------------------------------------------------------------------------------
 *
 * We could split this into more threads based on the user's environment. For example:
 *
 * Thread #1 ... #2: Dithers a specific frame. (We can dither 2 frames at once since
 * we have 2 threads now instead of just 1)
 * Thread #3: Sends the dithered frame data to the clients.
 *
 * Frame # | What to do:
 * --------|-------------------------------------
 * 1       | Thread #1 and #2 dithers this frame at the same time.
 * 2       | Thread #3 sends the dithered frame provided from thread #1/#2.
 * | Thread #1/#2 now dithers frame 2.
 *
 *
 * And so on, (chaining domino pattern) The advantage of having two threads dither is
 * that the result may be faster toreceive due to the parallel computations made on
 * the frame dithering. This will mean that the chance of blocking will likely less
 * occur due to how in an ideal environment (where it can support multiple threads
 * running at once at a good pace), it can provide the results faster.
 *
 * Since the actual dithering process will be made async/in parallel, encapsulate
 * the result in a Future&lt;int[]&gt;
 *
 * Have a FrameSender class (or whatever you wanna call it) with a Queue&lt;Future&lt;int[]&gt;&gt;,
 * I believe a LinkedList is the best option here
 *
 * Add the future data to the FrameSender queue orderly before/when the dithering starts
 * (not after it finishes, kinda defeats the purpose of using Futures here), then start
 * the process in parallel. That way, no matter when the frame is processed, since you
 * put them in order as they come in, they will be in order when you retrieve them
 *
 * In the sender, so long so it isn't empty, remove() the frame and Futures.getUnchecked(frame)
 * the data and send it, that way it will be sent as soon as it's done (basically blocking
 * the sender thread before sending it until the frame is processed; hopefully, them being
 * processed in parallel will keep them up and overcome any waiting overhead) (the class is
 * from Guava, it just wraps the checked Future#get() exceptions in an unchecked exception/error)
 *
 * </pre>
 *
 * This is currently a VideoPlayer in progress. It will not function at all. Please wait as I try to
 * develop it.
 */
public class ParallelVideoPlayer extends VideoPlayer {

  private final PacketHandler handler;
  private final DitherHolder holder;
  private final FilterLiteDither dither;
  private ByteBuffer frame;
  private int map;
  private int width, height;
  private int videoWidth;
  private int delay;
  private long lastUpdated;

  public ParallelVideoPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final Path file,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    super(library, file.toAbsolutePath().toString(), width, height, callback);
    handler = library.getHandler();
    holder = null;
    dither = new FilterLiteDither();
  }

  @Override
  public void start(final @NotNull Collection<? extends Player> players) {

  }

  public void handleFrame(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - lastUpdated >= delay) {
      lastUpdated = time;
      process(data);
    }
  }

  public CompletableFuture<?> process(final int[] data) {
    return CompletableFuture.supplyAsync(
            () -> frame = dither.ditherIntoMinecraft(data, videoWidth));
  }
}
