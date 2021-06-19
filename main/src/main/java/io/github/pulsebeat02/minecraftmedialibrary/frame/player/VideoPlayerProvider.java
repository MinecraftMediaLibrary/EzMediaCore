/*.........................................................................................
. Copyright © 2021 Brandon Li
.                                                                                        .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this
. software and associated documentation files (the “Software”), to deal in the Software
. without restriction, including without limitation the rights to use, copy, modify, merge,
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit
. persons to whom the Software is furnished to do so, subject to the following conditions:
.
. The above copyright notice and this permission notice shall be included in all copies
. or substantial portions of the Software.
.
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
. EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
. MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
. NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
. BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
. ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
. CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
.  SOFTWARE.
.                                                                                        .
.........................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame.player;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.chat.ChatCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.entity.EntityCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.highlight.BlockHighlightCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.map.MapDataCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.scoreboard.ScoreboardCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

public class VideoPlayerProvider {

  private static Constructor<?> BLOCK_HIGHLIGHT_PLAYER;
  private static Constructor<?> CHAT_PLAYER;
  private static Constructor<?> ENTITY_PLAYER;
  private static Constructor<?> MAP_PLAYER;
  private static Constructor<?> SCOREBOARD_PLAYER;

  static {
    final String base = "io.github.pulsebeat02.minecraftmedialibrary.frame.player";
    final String name;
    if (RuntimeUtilities.isLinux()) {
      if (ImmutableSet.of("amd64", "i386", "aarch64", "armhf")
          .contains(RuntimeUtilities.getCpuArchitecture())) {
        name = String.format("%s.ffmpeg.", base);
      } else {
        name = String.format("%s.jcodec.", base);
      }
    } else {
      name = String.format("%s.vlc.", base);
    }
    try {
      BLOCK_HIGHLIGHT_PLAYER = Class.forName(name + "BlockHighlightPlayer").getConstructors()[0];
      CHAT_PLAYER = Class.forName(name + "ChatPlayer").getConstructors()[0];
      ENTITY_PLAYER = Class.forName(name + "EntityPlayer").getConstructors()[0];
      MAP_PLAYER = Class.forName(name + "MapPlayer").getConstructors()[0];
      SCOREBOARD_PLAYER = Class.forName(name + "ScoreboardPlayer").getConstructors()[0];
    } catch (final ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a new ChatPlayer from the arguments.
   *
   * @param library the library
   * @param url the url
   * @param callback the callback
   * @param width the width
   * @param height the height
   * @return the ChatPlayer
   */
  public static VideoPlayer createChatPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final ChatCallbackPrototype callback,
      final int width,
      final int height) {
    try {
      return (VideoPlayer) CHAT_PLAYER.newInstance(library, url, callback, width, height);
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates a new EntityPlayer from the arguments.
   *
   * @param library the library
   * @param url the url
   * @param callback the callback
   * @param width the width
   * @param height the height
   * @return the EntityPlayer
   */
  public static VideoPlayer createEntityPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final EntityCallbackPrototype callback,
      final int width,
      final int height) {
    try {
      return (VideoPlayer)
          ENTITY_PLAYER.newInstance(library, url, callback, callback.getLocation(), width, height);
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates a new BlockHighlightPlayer from the arguments.
   *
   * @param library the library
   * @param url the url
   * @param callback the callback
   * @param width the width
   * @param height the height
   * @return the Block Highlight Player
   */
  public static VideoPlayer createBlockHighlightPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final BlockHighlightCallbackPrototype callback,
      final int width,
      final int height) {
    try {
      return (VideoPlayer)
          BLOCK_HIGHLIGHT_PLAYER.newInstance(library, url, callback, width, height);
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates a new MapPlayer from the arguments.
   *
   * @param library the library
   * @param url the url
   * @param callback the callback
   * @param width the width
   * @param height the height
   * @return the MapPlayer
   */
  public static VideoPlayer createMapPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final MapDataCallbackPrototype callback,
      final int width,
      final int height) {
    try {
      return (VideoPlayer) MAP_PLAYER.newInstance(library, url, callback, width, height);
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates a new ScoreboardPlayer from the arguments.
   *
   * @param library the library
   * @param url the url
   * @param callback the callback
   * @param width the width
   * @param height the height
   * @return the ScoreboardPlayer
   */
  public static VideoPlayer createScoreboardPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final ScoreboardCallbackPrototype callback,
      final int width,
      final int height) {
    try {
      return (VideoPlayer) SCOREBOARD_PLAYER.newInstance(library, url, callback, width, height);
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }
}
